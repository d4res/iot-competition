const Base64 = require('js-base64');

function getPolicyEncode(policy) {
  // 传入表单上传的policy字段，对policy进行Base64编码
  const encodedPolicy = Base64.encode(JSON.stringify(policy));
  return encodedPolicy;
}

module.exports = getPolicyEncode;
