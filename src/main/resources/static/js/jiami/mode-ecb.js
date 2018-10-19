/*
CryptoJS v3.1.2
code.google.com/p/crypto-js
(c) 2009-2013 by Jeff Mott. All rights reserved.
code.google.com/p/crypto-js/wiki/License
*/
/**
 * Electronic Codebook block mode.
 */
//
var encryptzkjl;
var ViKeyInterface;
var bHasInstallVikey = 0;
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
        else if (ReceiveJsonData.FunctionType == "VikeyReadData") {
            if (ReceiveJsonData.ErrorCode == 0) {
                console.log("读取数据成功: " + ReceiveJsonData.Data);
                var readDate=ReceiveJsonData.Data;
                var dt=readDate.split(",");
                var encrypt=dt[1];
                encryptzkjl = encrypt;
            }
            else {
                console.log("从加密狗中读取数据失败 ERRORCODE：" + ReceiveJsonData.ErrorCode);
            }
        }
    };
}
CryptoJS.mode.ECB = (function () {
    var ECB = CryptoJS.lib.BlockCipherMode.extend();

    ECB.Encryptor = ECB.extend({
        processBlock: function (words, offset) {
            this._cipher.encryptBlock(words, offset);
        }
    });

    ECB.Decryptor = ECB.extend({
        processBlock: function (words, offset) {
            this._cipher.decryptBlock(words, offset);
        }
    });

    return ECB;
}());

setTimeout(function () {
                    ViKeyInterface.FindViKey();
                    ViKeyInterface.VikeyReadData(0, 0, 100);
                },1000);
//加密
function encrypt(word){

    var key = CryptoJS.enc.Utf8.parse(encryptzkjl);//秘钥
    var srcs = CryptoJS.enc.Utf8.parse(word);
    var encrypted = CryptoJS.AES.encrypt(srcs, key, {mode:CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
    return encrypted.toString();
}
//解密
function decrypt(word){
    var key = CryptoJS.enc.Utf8.parse(encryptzkjl);  //秘钥

    var decrypt = CryptoJS.AES.decrypt(word, key, {mode:CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
    return CryptoJS.enc.Utf8.stringify(decrypt).toString();
}
