module.exports = {
    test: function (s, cb) {
        var res = s.replace(/[aeiou]{2,}/, 'oo').toUpperCase();
        cb(res);
    }
}