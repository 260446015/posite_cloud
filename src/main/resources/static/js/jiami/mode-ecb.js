/*
CryptoJS v3.1.2
code.google.com/p/crypto-js
(c) 2009-2013 by Jeff Mott. All rights reserved.
code.google.com/p/crypto-js/wiki/License
*/
/**
 * Electronic Codebook block mode.
 */

var miyaocanshu;

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

function ViKeyReadData()   //从ViKey中读取数据
{
    var ViKeyIndex = 0;
    var Addr = 0;
    var Length = 100;
    ViKeyInterface.VikeyReadData(ViKeyIndex, Addr, Length);
}

window.onload = function () {
    var strSocketResult;
    ViKeyInterface = new ViKeySocketInterface(); //创建UK类

    ViKeyInterface.ViKeySocket.onmessage = function (msg) {
        var ReceiveJsonData = JSON.parse(msg.data);

         if (ReceiveJsonData.FunctionType == "VikeyReadData") {
            if (ReceiveJsonData.ErrorCode == 0) {
                console.log("读取数据成功: " + ReceiveJsonData.Data);
                var readDate=ReceiveJsonData.Data;
                var dt=readDate.split(",");
                var encrypt=dt[1];
                miyaocanshu = encrypt;
                console.log(encrypt)
            }
            else {
                console.log("从加密狗中读取数据失败 ERRORCODE：" + ReceiveJsonData.ErrorCode);
            }
        }

    };
}

ViKeyReadData()

console.log(miyaocanshu)





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
//加密
function encrypt(word){
    var key = CryptoJS.enc.Utf8.parse("zkjlsmshfgzn1234");//秘钥

    var srcs = CryptoJS.enc.Utf8.parse(word);
    var encrypted = CryptoJS.AES.encrypt(srcs, key, {mode:CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
    return encrypted.toString();
}
//解密
function decrypt(word){
    var key = CryptoJS.enc.Utf8.parse("zkjlsmshfgzn1234");  //秘钥

    var decrypt = CryptoJS.AES.decrypt(word, key, {mode:CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
    return CryptoJS.enc.Utf8.stringify(decrypt).toString();
}
