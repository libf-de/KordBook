package de.libf.kordbook.data.tools

class Md5Tool {



    private fun _md5cycle(x: IntArray, k: IntArray) {
        var a = x[0]
        var b = x[1]
        var c = x[2]
        var d = x[3]
        // ff()
        a += (b and c or b.inv() and d) + k[0] - 680876936
        a = (a shl 7 or a ushr 25) + b
        d += (a and b or a.inv() and c) + k[1] - 389564586
        d = (d shl 12 or d ushr 20) + a
        c += (d and a or d.inv() and b) + k[2] + 606105819
        c = (c shl 17 or c ushr 15) + d
        b += (c and d or c.inv() and a) + k[3] - 1044525330
        b = (b shl 22 or b ushr 10) + c
        a += (b and c or b.inv() and d) + k[4] - 176418897
        a = (a shl 7 or a ushr 25) + b
        d += (a and b or a.inv() and c) + k[5] + 1200080426
        d = (d shl 12 or d ushr 20) + a
        c += (d and a or d.inv() and b) + k[6] - 1473231341
        c = (c shl 17 or c ushr 15) + d
        b += (c and d or c.inv() and a) + k[7] - 45705983
        b = (b shl 22 or b ushr 10) + c
        a += (b and c or b.inv() and d) + k[8] + 1770035416
        a = (a shl 7 or a ushr 25) + b
        d += (a and b or a.inv() and c) + k[9] - 1958414417
        d = (d shl 12 or d ushr 20) + a
        c += (d and a or d.inv() and b) + k[10] - 42063
        c = (c shl 17 or c ushr 15) + d
        b += (c and d or c.inv() and a) + k[11] - 1990404162
        b = (b shl 22 or b ushr 10) + c
        a += (b and c or b.inv() and d) + k[12] + 1804603682
        a = (a shl 7 or a ushr 25) + b
        d += (a and b or a.inv() and c) + k[13] - 40341101
        d = (d shl 12 or d ushr 20) + a
        c += (d and a or d.inv() and b) + k[14] - 1502002290
        c = (c shl 17 or c ushr 15) + d
        b += (c and d or c.inv() and a) + k[15] + 1236535329
        b = (b shl 22 or b ushr 10) + c
        // gg()
        a += (b and d or c and b.inv()) + k[1] - 165796510
        a = (a shl 5 or a ushr 27) + b
        d += (a and c or b and a.inv()) + k[6] - 1069501632
        d = (d shl 9 or d ushr 23) + a
        c += (d and b or a and d.inv()) + k[11] + 643717713
        c = (c shl 14 or c ushr 18) + d
        b += (c and a or d and c.inv()) + k[0] - 373897302
        b = (b shl 20 or b ushr 12) + c
        a += (b and d or c and b.inv()) + k[5] - 701558691
        a = (a shl 5 or a ushr 27) + b
        d += (a and c or b and a.inv()) + k[10] + 38016083
        d = (d shl 9 or d ushr 23) + a
        c += (d and b or a and d.inv()) + k[15] - 660478335
        c = (c shl 14 or c ushr 18) + d
        b += (c and a or d and c.inv()) + k[4] - 405537848
        b = (b shl 20 or b ushr 12) + c
        a += (b and d or c and b.inv()) + k[9] + 568446438
        a = (a shl 5 or a ushr 27) + b
        d += (a and c or b and a.inv()) + k[14] - 1019803690
        d = (d shl 9 or d ushr 23) + a
        c += (d and b or a and d.inv()) + k[3] - 187363961
        c = (c shl 14 or c ushr 18) + d
        b += (c and a or d and c.inv()) + k[8] + 1163531501
        b = (b shl 20 or b ushr 12) + c
        a += (b and d or c and b.inv()) + k[13] - 1444681467
        a = (a shl 5 or a ushr 27) + b
        d += (a and c or b and a.inv()) + k[2] - 51403784
        d = (d shl 9 or d ushr 23) + a
        c += (d and b or a and d.inv()) + k[7] + 1735328473
        c = (c shl 14 or c ushr 18) + d
        b += (c and a or d and c.inv()) + k[12] - 1926607734
        b = (b shl 20 or b ushr 12) + c
        // hh()
        a += (b xor c xor d) + k[5] - 378558
        a = (a shl 4 or a ushr 28) + b
        d += (a xor b xor c) + k[8] - 2022574463
        d = (d shl 11 or d ushr 21) + a
        c += (d xor a xor b) + k[11] + 1839030562
        c = (c shl 16 or c ushr 16) + d
        b += (c xor d xor a) + k[14] - 35309556
        b = (b shl 23 or b ushr 9) + c
        a += (b xor c xor d) + k[1] - 1530992060
        a = (a shl 4 or a ushr 28) + b
        d += (a xor b xor c) + k[4] + 1272893353
        d = (d shl 11 or d ushr 21) + a
        c += (d xor a xor b) + k[7] - 155497632
        c = (c shl 16 or c ushr 16) + d
        b += (c xor d xor a) + k[10] - 1094730640
        b = (b shl 23 or b ushr 9) + c
        a += (b xor c xor d) + k[13] + 681279174
        a = (a shl 4 or a ushr 28) + b
        d += (a xor b xor c) + k[0] - 358537222
        d = (d shl 11 or d ushr 21) + a
        c += (d xor a xor b) + k[3] - 722521979
        c = (c shl 16 or c ushr 16) + d
        b += (c xor d xor a) + k[6] + 76029189
        b = (b shl 23 or b ushr 9) + c
        a += (b xor c xor d) + k[9] - 640364487
        a = (a shl 4 or a ushr 28) + b
        d += (a xor b xor c) + k[12] - 421815835
        d = (d shl 11 or d ushr 21) + a
        c += (d xor a xor b) + k[15] + 530742520
        c = (c shl 16 or c ushr 16) + d
        b += (c xor d xor a) + k[2] - 995338651
        b = (b shl 23 or b ushr 9) + c
        // ii()
        a += (c xor (b or d.inv())) + k[0] - 198630844
        a = (a shl 6 or a ushr 26) + b
        d += (b xor (a or c.inv())) + k[7] + 1126891415
        d = (d shl 10 or d ushr 22) + a
        c += (a xor (d or b.inv())) + k[14] - 1416354905
        c = (c shl 15 or c ushr 17) + d
        b += (d xor (c or a.inv())) + k[5] - 57434055
        b = (b shl 21 or b ushr 11) + c
        a += (c xor (b or d.inv())) + k[12] + 1700485571
        a = (a shl 6 or a ushr 26) + b
        d += (b xor (a or c.inv())) + k[3] - 1894986606
        d = (d shl 10 or d ushr 22) + a
        c += (a xor (d or b.inv())) + k[10] - 1051523
        c = (c shl 15 or c ushr 17) + d
        b += (d xor (c or a.inv())) + k[1] - 2054922799
        b = (b shl 21 or b ushr 11) + c
        a += (c xor (b or d.inv())) + k[8] + 1873313359
        a = (a shl 6 or a ushr 26) + b
        d += (b xor (a or c.inv())) + k[15] - 30611744
        d = (d shl 10 or d ushr 22) + a
        c += (a xor (d or b.inv())) + k[6] - 1560198380
        c = (c shl 15 or c ushr 17) + d
        b += (d xor (c or a.inv())) + k[13] + 1309151649
        b = (b shl 21 or b ushr 11) + c
        a += (c xor (b or d.inv())) + k[4] - 145523070
        a = (a shl 6 or a ushr 26) + b
        d += (b xor (a or c.inv())) + k[11] - 1120210379
        d = (d shl 10 or d ushr 22) + a
        c += (a xor (d or b.inv())) + k[2] + 718787259
        c = (c shl 15 or c ushr 17) + d
        b += (d xor (c or a.inv())) + k[9] - 343485551
        b = (b shl 21 or b ushr 11) + c
        x[0] += a
        x[1] += b
        x[2] += c
        x[3] += d
    }

    private var _dataLength = 0
    private var _bufferLength = 0

    private val _state = IntArray(4)
    private val _buffer = ByteArray(68)




    fun start() {

    }
}