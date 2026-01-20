// Node.js 兼容性修复：util.isRegExp polyfill
// 在 Node.js 12+ 版本中，util.isRegExp 已被移除
// 这个文件需要在项目入口处引入

const util = require('util')

if (!util.isRegExp) {
  util.isRegExp = function(obj) {
    return Object.prototype.toString.call(obj) === '[object RegExp]'
  }
}

module.exports = util
