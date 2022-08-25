const Crypto = require('crypto-js');
const Base64 = require('js-base64');

function getSignature(policyEncoded, SecretKey){
  // 利用SK对Base64编码后的policy结果进行HMAC-SHA1签名计算
  const bytes = Crypto.HmacSHA1(policyEncoded, SecretKey);
  // 对计算结果进行Base64编码，得到最终的签名信息
  const signature = Crypto.enc.Base64.stringify(bytes);
  return signature;
}

module.exports = getSignature;