
var ViKeyInterface;
var bHasInstallVikey = 0;

function IsInstallVikey() {
    if (bHasInstallVikey == 0) {
        return layer.msg("尚未安装PKI驱动，或PKI驱动尚未正常运行");
    }
    else {
        console.log("PKI驱动工作正常");
    }
}

window.onload = function () {
    var strSocketResult;
    ViKeyInterface = new ViKeySocketInterface(); //创建UK类

    ViKeyInterface.ViKeySocket.onmessage = function (msg) {
        var ReceiveJsonData = JSON.parse(msg.data);

        if (ReceiveJsonData.FunctionType == "VikeyFind") {
            if (ReceiveJsonData.ErrorCode == 0) {
                console.log("已找到ViKey加密锁数量：" + ReceiveJsonData.Count);
                login();
            }
            else {
                console.log("查找失败 ERRORCODE：" + ReceiveJsonData.ErrorCode);
                return layer.msg("尚未发现PKI驱动，或PKI驱动尚未正常运行");
            }
        }
        else if (ReceiveJsonData.FunctionType == "CheckInstall") {
            //alert("CheckInstall");
            if (ReceiveJsonData.ErrorCode == 0) {
                bHasInstallVikey = 1;
                //IsInstallVikey();
            }
        }
        else if (ReceiveJsonData.FunctionType == "VikeyUserLogin") {
            if (ReceiveJsonData.ErrorCode == 0) {
                console.log("用户登录成功");
                ViKeyReadData();
            }
            else {
                layer.closeAll();
                return layer.msg("用户登录失败");
                $('#member_password').hide();
                console.log("用户权限登陆失败 ERRORCODE：" + ReceiveJsonData.ErrorCode);
            }
        }
        else if (ReceiveJsonData.FunctionType == "VikeyAdminLogin") {
            if (ReceiveJsonData.ErrorCode == 0) {
                console.log("管理员权限登陆成功");
            }
            else {
                console.log("管理员权限登陆失败 ERRORCODE：" + ReceiveJsonData.ErrorCode);
            }
        }

        else if (ReceiveJsonData.FunctionType == "VikeyReadData") {
            if (ReceiveJsonData.ErrorCode == 0) {
                console.log("读取数据成功: " + ReceiveJsonData.Data);
                var readDate=ReceiveJsonData.Data;
                var dt=readDate.split(",");
                var encrypt=dt[1];
                //加密
                var key = CryptoJS.enc.Utf8.parse("13223wrwe4345678");
                var srcs = CryptoJS.enc.Utf8.parse(encrypt);
                var encrypted = CryptoJS.AES.encrypt(srcs, key, {mode:CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
                sessionStorage.setItem("userid",encrypted.toString());
                $.ajax({
                    url: "/login",
                    type: "post",
                    data: {
                        password: dt[0]

                    },
                    xhrFields: {
                        withCredentials: true
                    },

                    success: function (res) {
                        console.log(res);
                        if (res.code != 0) {
                            return layer.msg(res.message, {anim: 6});
                        }
                        sessionStorage.setItem("zdryscuser", JSON.stringify(res.data));
                        window.location = "index.html";
                    }
                });
            }
            else {
                console.log("从加密狗中读取数据失败 ERRORCODE：" + ReceiveJsonData.ErrorCode);
            }
        }
        else if (ReceiveJsonData.FunctionType == "VikeyWriteData") {
            if (ReceiveJsonData.ErrorCode == 0) {
                console.log("写入数据成功: " + FM.EditWriteData.value);
            }
            else {
                console.log("向加密狗中写入数据失败 ERRORCODE：" + ReceiveJsonData.ErrorCode);
            }
        }
        else if (ReceiveJsonData.FunctionType == "Vikey3DesEncrypt") {
            if (ReceiveJsonData.ErrorCode == 0) {
                console.log("3DES加密生成密文：" + ReceiveJsonData.Result);
            }
            else {
                console.log("3DES加密失败 ERRORCODE：" + ReceiveJsonData.ErrorCode);
            }
        }
        else if (ReceiveJsonData.FunctionType == "Vikey3DesDecrypt") {
            if (ReceiveJsonData.ErrorCode == 0) {
                console.log("3DES解密生成密文：" + ReceiveJsonData.Result);
            }
            else {
                console.log("3DES解密失败 ERRORCODE：" + ReceiveJsonData.ErrorCode);
            }
        }

    };
}

function ViKeyFind()    //查找加密锁
{
    ViKeyInterface.FindViKey();
    again();
}

function again()    //再次获取加密狗
{
    ViKeyInterface.FindViKey();
}

function login()    //调用登录
{
    //prompt层
    var promptlay;
    promptlay = layer.prompt({id:'promitlist',title: '请输入PKI验证密码', formType: 1}, function (pass, index) {
        layer.close(promptlay);
        layer.load(1, {
            shade: [0.2,'#000'] //0.1透明度的白色背景
        });
        ViKeyUserLogon(pass);
    });
}

function ViKeyHID()   //获取硬件序列号
{
    var ViKeyIndex = 0;
    ViKeyInterface.VikeyGetHID(ViKeyIndex);
}

function ViKeyUserLogon(password)   //以用户权限登录
{
    var ViKeyIndex = 0;
    ViKeyInterface.VikeyUserLogin(ViKeyIndex, password);

}

function ViKeyAdminLogon()   //以管理员权限登录
{
    var ViKeyIndex = 0;
    ViKeyInterface.VikeyAdminLogin(ViKeyIndex, zkjladminpassword);
    ViKeyReadData();
}

function ViKeyReadData()   //从ViKey中读取数据
{
    var ViKeyIndex = 0;
    var Addr = 0;
    var Length = 100;
    ViKeyInterface.VikeyReadData(ViKeyIndex, Addr, Length);

}

function Des3Encrypt()   //3Des加密
{
    var FM = window.document.ViKeyForm;
    var ViKeyIndex = 0;

    if (FM.edtDesEncrypt.value.length != 24) {
        alert("加密长度必须为24个字符");
    }
    else {
        ViKeyInterface.Vikey3DesEncrypt(ViKeyIndex, 24, FM.edtDesEncrypt.value);
    }
}

$(".ctbottom").click(function () {
    var pwd = $("#UserPin2").val();
    if (pwd == null || pwd == "") {
        return layer.msg("尚未安装PKI驱动，或PKI驱动尚未正常运行");
        ViKeyFind();
    } else {
        ViKeyUserLogon(pwd);
    }

});

$(".btnpki").click(function () {
    ViKeyFind();
});