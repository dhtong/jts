package com.vividsolutions.jts.polytriangulate;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

public class EarClipperTest extends TestCase {
    protected WKTReader reader = new WKTReader();
    protected WKTWriter writer = new WKTWriter();
    static final double COMPARISON_TOLERANCE = 1.0e-7;
    GeometryFactory fact = new GeometryFactory();

    public EarClipperTest(String name) {
        super(name);
    }

    public void testTriangle() throws ParseException {
        String triangleStr = "POLYGON ((10 20, 10 10, 20 20, 10 20))";
        String expected = "GEOMETRYCOLLECTION (POLYGON ((10 10, 20 20, 10 20, 10 10)))";
        runCompare(triangleStr, expected);
    }

    public void testSquare() throws ParseException {
        String squareStr = "POLYGON ((3 -1, 2 2, 5 3, 6 0, 3 -1))";
        String expected = "GEOMETRYCOLLECTION (POLYGON ((2 2, 5 3, 6 0, 2 2)), "
                + "POLYGON ((2 2, 3 -1, 6 0, 2 2)))";
        runCompare(squareStr, expected);
    }

    public void testRectangle() throws ParseException {
        String rectStr = "POLYGON ((10 20, 10 10, 30 10, 30 20, 10 20))";
        String expected = "GEOMETRYCOLLECTION (POLYGON ((10 10, 30 20, 30 10, 10 10)), "
                + "POLYGON ((10 10, 30 20, 10 20, 10 10)))";
        runCompare(rectStr, expected);
    }

    public void testRandomConvex() throws ParseException {
        String poly = "POLYGON ((150 330, 250 370, 324 366, 380 340, 430 150, 300 110, 130 220, 150 330))";
        unionEqualsOrigin(poly);
    }

    public void testRandomConcave() throws ParseException {
        String poly = "POLYGON ((190 400, 200 150, 210 380, 320 370, 260 290, 430 250, 260 260, 475 144, 180 20, 40 190, 20 310, 140 320, 190 400))";
        unionEqualsOrigin(poly);
    }

    // includes holes attached to shell edge.
    public void testUltimate() throws ParseException {
        String poly = "POLYGON ((220 390, 180 390, 169 387, 159 387, 148 387, 135 384, 122 382, 113 377, 100 374, 88 368, 75 361, 62 352, 52 343, 42 331, 32 318, 27 308, 23 296, 19 281, 18 270, 18 256, 18 245, 18 223, 21 212, 24 201, 28 189, 31 178, 34 163, 37 151, 40 140, 50 141, 51 153, 51 164, 47 174, 44 186, 39 195, 35 205, 34 216, 31 230, 31 241, 31 252, 31 263, 31 273, 31 283, 35 294, 41 304, 47 312, 55 318, 65 320, 66 309, 60 300, 52 294, 46 285, 42 275, 39 265, 39 251, 40 239, 42 228, 46 217, 52 207, 58 198, 63 189, 64 177, 64 166, 64 155, 64 143, 63 133, 58 124, 48 121, 39 129, 36 139, 33 149, 28 158, 23 167, 15 160, 15 148, 15 137, 16 126, 19 116, 24 102, 29 88, 33 77, 39 65, 46 54, 55 49, 63 43, 73 37, 82 32, 93 27, 104 24, 115 22, 125 21, 135 19, 149 17, 159 16, 167 10, 177 10, 189 10, 199 11, 210 13, 220 16, 229 21, 219 25, 209 25, 199 26, 189 26, 179 26, 168 26, 157 26, 147 27, 137 28, 126 31, 116 35, 106 39, 96 44, 85 50, 76 55, 68 61, 60 68, 54 76, 46 83, 40 92, 34 100, 29 109, 26 119, 23 129, 31 123, 39 116, 47 110, 54 102, 59 93, 67 86, 75 79, 85 73, 95 69, 105 65, 115 62, 127 58, 137 53, 147 52, 157 50, 168 48, 179 44, 189 41, 201 38, 211 33, 221 31, 231 28, 242 25, 253 24, 264 24, 278 23, 289 22, 299 21, 311 21, 322 21, 335 27, 344 33, 353 41, 360 51, 366 64, 371 76, 375 86, 388 100, 397 108, 408 120, 415 129, 424 139, 432 150, 438 159, 446 173, 452 186, 456 199, 458 210, 460 221, 460 233, 460 249, 460 261, 460 274, 460 286, 455 302, 446 316, 435 332, 424 342, 415 347, 405 352, 396 358, 383 365, 371 371, 360 375, 350 378, 340 381, 328 382, 317 385, 306 385, 295 386, 284 387, 274 387, 272 377, 279 369, 290 366, 300 365, 311 365, 321 365, 332 366, 342 366, 353 366, 364 366, 375 363, 384 358, 392 352, 402 348, 410 341, 418 334, 426 326, 432 318, 437 309, 438 299, 440 289, 432 295, 426 303, 421 312, 413 319, 403 327, 393 329, 383 335, 374 341, 363 345, 352 348, 342 349, 331 349, 321 350, 311 350, 300 352, 290 354, 281 360, 271 364, 262 369, 254 376, 244 378, 234 378, 224 378, 212 377, 201 376, 188 374, 178 371, 167 368, 156 365, 147 360, 137 357, 126 352, 116 348, 105 344, 93 342, 83 340, 74 335, 68 343, 76 350, 86 354, 95 359, 105 361, 115 365, 126 368, 137 370, 148 374, 158 376, 250 390, 220 390), (90 260, 125 327, 180 260, 90 260), (125 327, 190 341, 195 261, 190 261, 125 327), (90 231, 81 239, 76 248, 74 258, 74 269, 74 280, 79 290, 85 298, 92 307, 100 315, 109 321, 117 328, 126 333, 137 337, 147 341, 158 343, 170 344, 180 344, 190 344, 200 342, 208 335, 214 326, 217 315, 217 305, 217 295, 214 285, 211 275, 211 265, 218 273, 220 283, 222 293, 225 303, 227 313, 227 323, 226 333, 221 342, 211 347, 202 353, 192 357, 182 357, 172 357, 162 356, 151 351, 142 346, 132 343, 123 338, 113 334, 104 329, 95 324, 87 317, 79 310, 72 302, 68 292, 67 282, 67 272, 69 262, 69 252, 70 242, 72 232, 73 220, 83 220, 93 221, 103 224, 112 229, 118 237, 110 244, 100 246, 90 248, 98 241, 107 236, 107 235, 90 231), (130 250, 130 173.5, 160 250, 143.5 250, 130 250), (130 220, 105 220, 105 177.5, 130 220), (80 200, 82 190, 84 178, 86 167, 91 158, 99 152, 109 154, 114 163, 118 173, 119 183, 121 173, 125 163, 133 156, 144 161, 147 172, 148 183, 150 193, 156 185, 158 175, 159 165, 160 155, 155 145, 145 144, 135 147, 144 140, 154 136, 162 130, 167 121, 166 111, 160 102, 152 96, 142 94, 132 96, 130 106, 130 116, 126 126, 121 135, 113 127, 112 117, 109 107, 104 98, 94 95, 84 96, 76 103, 76 113, 82 122, 90 129, 98 135, 108 139, 98 141, 88 141, 78 142, 74 152, 71 162, 71 172, 70 180, 80 200), (250 340, 170 90, 180 90, 250 340), (280 230, 300 290, 270 280, 250 260, 250 230, 271 225, 280 230), (280 300, 271 295, 261 291, 253 283, 248 273, 242 264, 236 256, 234 246, 234 236, 235 226, 240 217, 246 209, 257 205, 267 205, 278 206, 288 212, 296 218, 301 228, 304 238, 309 250, 311 260, 312 270, 312 280, 311 290, 303 296, 293 299, 301 306, 311 306, 319 298, 325 289, 328 279, 329 268, 329 256, 328 246, 324 235, 320 225, 314 216, 306 206, 297 198, 289 192, 279 187, 269 183, 259 182, 249 182, 241 189, 235 197, 229 205, 225 215, 224 225, 223 235, 226 245, 230 255, 233 265, 238 274, 243 283, 248 292, 256 300, 264 307, 275 311, 280 310, 280 300), (450 300, 447 289, 447 279, 447 269, 447 259, 446 249, 445 238, 441 228, 437 218, 433 208, 427 198, 422 188, 416 178, 411 168, 405 158, 399 150, 392 141, 387 132, 381 124, 391 124, 398 132, 404 141, 410 150, 417 161, 423 170, 428 179, 434 187, 440 195, 443 206, 446 217, 448 227, 452 237, 454 247, 454 275, 450 300), (290 160, 252 135, 320 140, 290 160), (320 120, 270 90, 340 80, 320 120), (250 50, 300 70, 315 42, 250 50), (200 100, 224 146, 230 100, 200 100), (200 50, 230 60, 230 50, 200 50), (220 80, 280 80, 280 70, 220 80), (370 310, 365 301, 364 291, 372 285, 382 283, 392 285, 395 295, 389 303, 381 310, 380 310, 370 310), (350 280, 344 272, 340 262, 345 253, 355 248, 365 247, 375 252, 378 262, 376 272, 360 280, 350 280), (380 230, 370 226, 366 216, 369 206, 376 198, 386 193, 397 193, 404 202, 407 213, 407 223, 400 230, 380 230), (420 270, 414 261, 413 251, 414 241, 423 236, 433 236, 438 246, 442 257, 441 267, 430 270, 420 270), (390 270, 391 260, 386 250, 378 244, 368 241, 356 239, 345 235, 337 227, 332 218, 331 207, 331 196, 338 188, 345 180, 354 175, 364 171, 376 170, 386 170, 388 180, 378 180, 368 180, 358 181, 350 187, 345 196, 344 206, 344 216, 351 224, 361 228, 370 233, 381 238, 390 243, 396 252, 401 262, 407 270, 411 280, 413 290, 400 280, 390 270), (334 136, 324 136, 314 135, 304 132, 294 129, 284 127, 274 125, 264 124, 254 122, 246 115, 241 106, 240 96, 245 87, 249 97, 255 105, 263 112, 273 116, 283 119, 293 121, 303 124, 313 127, 323 128, 332 123, 340 117, 346 109, 347 99, 347 89, 348 79, 341 71, 330 71, 320 71, 310 73, 312 63, 322 60, 332 60, 342 61, 349 69, 355 77, 358 87, 358 97, 358 107, 358 117, 350 124, 340 130, 334 136))";
        unionEqualsOrigin(poly);
    }

    /*
     * Martin's square cases
     */
    public void testCraySquare() throws ParseException {
        String poly = "POLYGON ((100 400, 500 400, 500 100, 100 100, 100 400), (150 350, 200 350, 200 300, 150 300, 150 350), (150 250, 200 250, 200 200, 150 200, 150 250), (150 170, 200 170, 200 130, 150 130, 150 170), (225 325, 270 325, 270 290, 225 290, 225 325), (230 270, 270 270, 270 230, 230 230, 230 270), (230 200, 270 200, 270 160, 230 160, 230 200), (300 160, 340 160, 340 130, 300 130, 300 160), (300 230, 340 230, 340 180, 300 180, 300 230), (300 300, 340 300, 340 260, 300 260, 300 300), (300 370, 340 370, 340 330, 300 330, 300 370), (360 330, 410 330, 410 290, 360 290, 360 330), (360 260, 410 260, 410 220, 360 220, 360 260), (375 185, 420 185, 420 150, 375 150, 375 185), (430 300, 470 300, 470 240, 430 240, 430 300), (430 380, 469 380, 469 335, 430 335, 430 380), (440 210, 470 210, 470 170, 440 170, 440 210), (440 140, 470 140, 470 110, 440 110, 440 140), (220 380, 270 380, 270 350, 220 350, 220 380), (125 282, 211 282, 211 265, 125 265, 125 282), (225 137, 278 137, 278 115, 225 115, 225 137))";
        unionEqualsOrigin(poly);
    }

    public void testCrayCircle() throws ParseException {
        String poly = "POLYGON ((20 0, 20 420, 470 420, 470 0, 20 0), (60 250, 80 240, 95 249, 100 270, 80 280, 60 270, 60 250), (64 175, 75 163, 90 160, 100 170, 100 190, 80 200, 70 190, 64 175), (90 110, 100 90, 120 90, 130 100, 130 120, 120 130, 100 130, 90 110), (106 324, 115 312, 130 310, 140 320, 140 340, 127 345, 110 340, 106 324), (140 210, 149 205, 160 210, 160 220, 160 230, 150 230, 140 220, 140 210), (145 364, 155 352, 170 350, 185 363, 180 380, 164 385, 150 380, 145 364), (150 240, 162 235.5, 170 240, 170 250, 158 257.5, 153 253, 150 240), (155 57, 170 40, 190 40, 194 54, 190 70, 180 80, 170 80, 160 70, 155 57), (160 270, 170 260, 180 260, 190 270, 190 280, 180 290, 170 290, 163 285, 160 280, 160 270), (170 170, 177 161.5, 184 159, 189.5 162.5, 193.5 170.5, 190 180, 181 184.5, 174.5 179.5, 170 170), (190 320, 200 310, 204 315, 208 325, 200 330, 191 325, 190 320), (199 215, 200 210, 206 205.5, 210 210, 213 215.5, 210 220, 203.5 225, 200 220, 199 215), (200 140, 205 131.5, 210 130, 216.5 134, 220 140, 215 150, 206.5 153, 200.5 146.5, 200 140), (200 370, 210 360, 230 360, 240 380, 235 395, 220 400, 210 390, 200 370), (206.5 250.5, 207 245.5, 213 241, 217 243, 219.5 247, 217.5 253.5, 212 257, 206.5 250.5), (221 194, 226.5 189.5, 233.5 193, 234 201, 227.5 205.5, 223.5 202, 221 194), (221 263, 223 257.5, 228 257.5, 230 260, 230 265.5, 225 269, 221 267, 221 263), (230 230, 232.5 226, 235 229.5, 232.5 234.5, 230 230), (230 310, 240 310, 246.66666666666666 315.55555555555554, 245.55555555555554 324.44444444444446, 240 330, 230 320, 230 310), (232 45, 240 30, 258 25, 271 35, 272 55, 255 64, 240 60, 232 45), (236.5 272, 241.5 266, 248 266, 250 270, 252 276.5, 245.5 280, 240 280, 236.5 272), (238.5 124, 244 117, 252.5 115, 260 120, 260 130, 255.5 135.5, 247.5 135.5, 243.5 132.5, 238.5 124), (240 210, 243 209.5, 245 213.5, 242 217, 240 215, 240 210), (243 227, 247 219.5, 251.5 217, 257 221.5, 257 228, 249.5 233.5, 243 227), (246.5 180.5, 253 177, 260 180, 259 186.5, 257 190, 250 190, 246.5 186, 246.5 180.5), (247.5 245.5, 250 240, 252 243, 250 247, 247.5 245.5), (254.5 208, 255 204, 258 204, 259 207, 257 211, 254.5 208), (255 376, 265 365, 280 360, 290 370, 290 390, 280 400, 270 400, 260 390, 255 376), (260 310, 270 300, 280 300, 285.55555555555554 305.55555555555554, 290 320, 275.55555555555554 324.44444444444446, 270 320, 260 310), (261.5 237, 265 232.5, 267.5 236, 265 240, 261.5 237), (264 263, 266.5 258.5, 270 260, 275 266.5, 270 270, 265 267, 264 263), (264.5 222.5, 266.5 217.5, 269 217.5, 270 220, 267.5 225, 264.5 222.5), (275.5 186.5, 280 180, 286.5 181, 288 186, 287.5 192, 281.5 194.5, 277.5 192, 275.5 186.5), (280 215.5, 283.5 212.5, 290 210, 293.5 214.5, 293.5 220.5, 286 222.5, 280 220, 280 215.5), (280 240, 283 236.5, 289.5 236, 290 240, 290 246.5, 284 249, 280 245.5, 280 240), (287 121, 295 110, 305 112, 308.5 122.5, 305.5 133.5, 297 134.5, 290 130, 287 121), (290 290, 300 280, 310 280, 320 290, 310 300, 300 300, 290 290), (310 40, 330 20, 350 30, 350 50, 340 60, 320 60, 310 40), (310 360, 320 350, 337 345, 352 355, 350 370, 340 380, 328 385, 315 378, 310 360), (320 150, 325.5 140, 330 140, 338 146, 338 157.5, 330 160, 324 158, 320 150), (320 243.5, 328 238, 340 240, 340 250, 337 259, 327.5 259.5, 320 254.5, 320 243.5), (328 183, 333 176, 341 175, 346.5 180.5, 346 189.5, 338 194.5, 332 192, 328 183), (330 210, 340 210, 345.5 216, 343.5 224, 337 226, 330 220, 330 210), (346 296, 350 280, 365 273, 380 270, 390 280, 400 290, 385 307, 368 315, 355 309, 346 296), (360 70, 380 60, 400 60, 410 80, 400 100, 380 100, 360 90, 360 70), (380 150, 390 130, 410 130, 420 150, 415 164, 400 170, 386 164, 380 150), (390 220, 400 200, 414 204, 430 220, 430 240, 410 250, 390 240, 390 220))";
        unionEqualsOrigin(poly);
    }

    public void testCrayCurveOne() throws ParseException {
        String poly = "POLYGON ((-17 179, -17 194, -17 207, -15 223, -10 239, -5 254, 4 272, 15 289, 28 306, 43 323, 61 337, 71 345, 82 353, 100 365, 112 378, 120 387, 133 397, 144 405, 161 414, 171 418, 188 425, 207 428, 225 428, 244 428, 259 428, 270 430, 310 440, 418 435, 424 426, 432 418, 438 409, 442 399, 449 387, 449 377, 449 365, 449 351, 449 338, 449 327, 449 316, 449 306, 449 288, 448 277, 446 266, 445 254, 444 242, 440 225, 434 211, 425 201, 415 194, 403 189, 393 186, 382 183, 367 181, 355 179, 344 178, 329 176, 315 174, 301 172, 285 170, 275 169, 260 167, 247 165, 230 164, 212 163, 195 161, 181 158, 170 157, 160 157, 150 157, 138 158, 127 159, 115 161, 101 164, 91 167, 74 175, 62 179, 57 190, 57 201, 58 215, 63 225, 70 234, 76 243, 82 256, 90 270, 96 278, 105 288, 111 296, 119 303, 129 311, 139 320, 151 330, 163 339, 173 345, 181 351, 191 357, 201 362, 215 368, 227 374, 238 381, 247 386, 251 397, 244 405, 234 410, 223 414, 212 415, 200 415, 190 412, 181 406, 172 401, 165 393, 157 384, 150 373, 143 364, 133 358, 123 350, 107 337, 98 330, 90 322, 74 310, 66 303, 50 289, 38 277, 29 266, 21 257, 16 246, 8 239, 5 226, 5 215, 5 205, 5 195, 7 185, 14 177, 21 169, 29 161, 40 149, 46 141, 54 135, 67 128, 77 122, 90 115, 102 110, 116 106, 132 105, 147 105, 159 105, 175 105, 192 105, 206 107, 216 109, 230 113, 241 117, 250 123, 260 128, 269 133, 281 137, 291 138, 302 138, 313 138, 324 138, 338 137, 350 135, 360 133, 371 127, 383 123, 393 119, 402 114, 413 108, 422 100, 430 91, 431 80, 426 70, 418 64, 409 58, 399 52, 388 47, 375 42, 363 37, 353 37, 342 38, 332 40, 325 48, 323 58, 322 68, 322 79, 321 90, 317 102, 305 107, 297 99, 290 90, 283 78, 272 68, 262 59, 253 53, 244 47, 234 38, 224 31, 214 33, 200 35, 188 37, 173 39, 157 42, 142 45, 122 50, 112 51, 101 51, 88 52, 74 54, 63 58, 53 64, 44 69, 34 77, 25 84, 13 94, 5 102, 0 111, -3 121, -5 131, -9 143, -12 155, -16 166, -17 179), (20 128, 23 116, 30 104, 36 94, 45 86, 54 80, 64 75, 76 71, 87 70, 97 70, 107 69, 118 68, 130 68, 140 68, 152 70, 162 73, 170 79, 160 79, 150 79, 140 79, 127 79, 117 81, 104 84, 92 86, 80 88, 72 94, 64 101, 56 109, 48 117, 40 123, 30 140, 20 150, 20 138, 20 128), (65 193, 89 175, 75 196, 79 225, 65 193), (97 208, 100 197, 104 187, 112 181, 122 178, 132 173, 145 169, 158 164, 170 168, 183 173, 198 181, 207 186, 216 192, 227 200, 236 207, 248 208, 254 200, 262 192, 271 185, 282 184, 293 184, 304 184, 316 186, 328 191, 339 196, 349 202, 358 208, 367 214, 375 222, 385 227, 395 233, 403 242, 405 253, 404 266, 402 277, 397 295, 395 306, 390 316, 381 325, 373 332, 362 338, 350 344, 339 348, 329 349, 316 349, 306 349, 296 346, 286 343, 277 338, 266 332, 268 321, 278 324, 288 328, 304 328, 320 328, 332 327, 344 323, 353 316, 358 303, 358 293, 357 280, 355 268, 350 257, 344 246, 335 236, 327 230, 318 225, 306 219, 292 216, 280 216, 265 217, 252 222, 246 230, 246 241, 246 251, 244 240, 235 231, 226 223, 215 216, 202 210, 189 209, 174 209, 158 212, 145 217, 134 226, 134 236, 141 246, 148 256, 156 264, 165 274, 176 283, 190 293, 200 300, 180 290, 162 297, 152 294, 142 286, 134 278, 125 268, 119 260, 113 250, 107 239, 102 230, 97 219, 97 208), (98 246, 104 254, 111 262, 119 270, 125 278, 134 284, 142 291, 150 297, 158 303, 167 308, 177 310, 187 313, 197 314, 208 312, 213 302, 213 292, 212 282, 208 272, 202 264, 195 255, 187 248, 180 240, 200 250, 209 259, 218 264, 224 273, 226 284, 225 294, 221 304, 216 313, 211 322, 201 324, 191 322, 181 320, 171 320, 161 318, 152 313, 143 307, 135 300, 126 292, 120 284, 112 276, 107 267, 101 256, 98 246), (175 329, 176.25 328.5, 178 328.5, 179 328.5, 180 328.5, 181.25 328.5, 182.5 328.5, 183.75 328.5, 185 328.5, 186.25 328.5, 187.5 328.75, 188.5 329, 189.5 329.25, 190.75 329.5, 191.75 329.75, 193 330, 194.25 330.25, 195.5 330.25, 196.5 330.25, 197.5 330.25, 198.75 330.25, 199.75 330.25, 200.75 330.25, 202.25 330.25, 203.5 330.25, 204.5 330.25, 205.5 330.25, 206.75 330.25, 207.75 330.25, 208.75 330.25, 209.75 330.25, 210.75 329.5, 211.5 328.75, 212.5 328, 213.25 327.25, 214 326.5, 214.75 325.75, 215.25 324.75, 215.75 323.75, 216.25 322.75, 216.5 321.75, 217 320.75, 217.25 319.75, 217.75 318.75, 218.5 317.5, 219.25 316.5, 220 315.25, 221 314.25, 221.75 313, 222.5 312.25, 223.25 311.5, 223.75 310.5, 224.5 309.5, 225 308.5, 225.25 307.5, 225.5 306.5, 225.5 305.5, 225.75 304.25, 226 303.25, 226.25 302, 226.5 301, 226.75 300, 227 298.75, 227.25 297.75, 227.5 296.75, 227.5 295.5, 227.5 294.5, 227.75 293.5, 228 292.5, 228 291.25, 228.5 290, 228.5 288.75, 228.75 287.75, 228.75 286.75, 229 285.75, 229.25 284.5, 229.25 283.25, 229.5 282.25, 229.5 281.25, 229.5 280.25, 229.5 279.25, 229.5 278, 229.5 277, 229.5 276, 229.5 275, 229.5 273.5, 229 272.5, 228.75 271.25, 228.25 270, 227.75 268.75, 227.5 267.75, 226.75 266.25, 225.75 264.75, 225 263.5, 223.5 261.5, 223 260.5, 221.75 258.75, 220.75 257.75, 219.75 257, 219.25 256, 218.5 255, 218 254, 217.25 253.25, 217 252.25, 216.5 251.25, 215.75 250.25, 215 249.25, 214.5 248.25, 213.75 247.5, 213.25 246.5, 214.25 247, 215.25 247.5, 216.25 247.5, 217.25 248, 218.25 248.75, 219.25 249.5, 220.25 250, 221 250.75, 222 251.5, 223.25 252.25, 224.25 253, 225.25 253.5, 226.75 253.75, 227.75 254.25, 229.25 254.5, 230.5 255, 232 255.5, 233.25 255.75, 234.25 256, 235.75 256.5, 237 256.75, 238.25 257.25, 239.25 257.5, 240.5 257.5, 241.75 257.75, 243 257.75, 244 257.75, 245.5 257.75, 246.5 257.5, 248 256.75, 249.25 256.25, 250 255.5, 250.75 254.25, 251.25 253, 251.25 251.75, 251.25 250.5, 251.25 249.5, 251.25 248.5, 251.25 247.5, 251.25 246.5, 251.25 245.5, 252.25 244.75, 253.25 245.25, 254 246.5, 254.5 247.5, 254.75 248.5, 255 249.75, 255 250.75, 254.75 251.75, 254.25 253, 253.75 254, 253.25 255, 252.5 256, 251.5 257, 250.75 257.75, 249.5 258.25, 248.5 258.5, 247 259, 246 259.5, 244.75 259.75, 243.75 260, 242.75 260, 241.75 260, 240.75 260.25, 239.75 260.25, 238.75 260.25, 237.75 260.25, 236.75 260.25, 235.75 260.25, 234.75 260, 233.5 259.75, 232.25 259.5, 231 259, 229.75 258.25, 228.75 257.75, 227.5 257.25, 226.5 256.75, 225.5 256.5, 226 257.5, 226.5 258.75, 227.25 259.5, 228 261, 228.75 261.75, 229.25 263, 229.75 264.25, 230.25 265.25, 230.75 266.25, 231.25 267.5, 231.75 268.5, 232 269.5, 232 270.5, 232.25 271.5, 232.5 272.5, 232.5 273.5, 232.5 274.5, 232.5 275.75, 232.75 277, 232.75 278.25, 232.75 279.5, 232.75 280.5, 232.75 281.5, 232.75 282.5, 232.5 283.5, 232.5 284.5, 232.5 285.5, 232.25 286.75, 232.25 287.75, 232.25 289, 232.25 290, 232 291.25, 231.75 292.25, 231.25 293.25, 231 294.75, 230.5 295.75, 230.25 297, 229.75 298.5, 229.5 299.5, 229.5 300.5, 229.25 301.5, 228.75 302.5, 228.75 303.5, 228.25 304.5, 228.25 305.5, 227.75 306.5, 227.5 307.5, 227.25 308.5, 226.75 309.5, 226.25 310.5, 226 311.5, 225.5 312.5, 225.25 313.5, 224.75 314.5, 224.75 315.5, 224.5 316.5, 223.75 317.75, 223.25 319.25, 222.75 321, 222.5 322.25, 222.5 323.25, 222.25 325.5, 222 327, 221.5 328, 221.25 329.25, 220.75 330.25, 220.25 331.5, 219.75 332.5, 219 333.25, 218.5 334.25, 217.75 335.25, 216.5 336.5, 215 337.5, 214 338, 212.75 338.25, 211.75 338.25, 210 338.5, 209 338.5, 207.75 338.5, 206.5 338.5, 205.25 338.5, 203.25 338.5, 201.75 338.5, 200.5 338.25, 198.5 338.25, 196.75 338.25, 195.5 338.25, 194.25 338.25, 192.75 338.25, 191.5 338.25, 190 338, 188.75 337.5, 182 334, 175 329), (209 54, 218 48, 228 46, 260 70, 230 60, 239 67, 249 78, 262 88, 270 97, 279 104, 289 109, 299 114, 309 117, 319 117, 329 117, 333 104, 334 93, 334 83, 337 73, 345 66, 355 66, 365 68, 369 80, 370 92, 367 103, 360 112, 352 118, 342 123, 332 127, 322 127, 312 127, 302 126, 291 122, 280 119, 270 114, 261 107, 253 100, 245 92, 238 84, 230 77, 225 68, 217 62, 209 54), (228 322, 229 312, 234 301, 239 291, 247 282, 257 277, 268 273, 280 270, 290 270, 300 273, 307 281, 310 290, 315 303, 304 298, 295 293, 285 291, 273 290, 262 290, 251 295, 244 304, 242 315, 242 326, 243 336, 247 346, 255 354, 265 361, 275 365, 285 367, 297 370, 307 371, 318 371, 329 371, 340 370, 351 367, 361 363, 372 358, 381 353, 391 349, 400 344, 409 336, 414 327, 417 317, 420 306, 420 295, 419 285, 414 276, 424 279, 431 287, 433 297, 433 308, 431 318, 427 328, 422 337, 414 343, 408 351, 400 358, 393 366, 383 372, 374 377, 364 379, 353 380, 343 380, 333 381, 323 381, 313 381, 303 381, 293 381, 283 379, 273 374, 263 370, 254 365, 246 359, 239 351, 233 342, 228 333, 228 322), (219.5 363.5, 230.5 364.5, 240 368, 248 374, 253 383.5, 256.5 394.5, 255.5 404.5, 248 411.5, 237 415, 226.5 417.5, 216.5 418, 206.5 418, 215.5 422.5, 225.5 423, 236 423, 246 421, 255.5 415.5, 261.5 407.5, 263 397.5, 262.5 387.5, 258 378.5, 251 371, 244 363.5, 235 358.5, 225 357.5, 207 359.5, 219.5 363.5), (285 429, 284 428.75, 283 428.75, 281.25 428.75, 280.25 428.75, 278.75 428, 277.75 427.25, 277 426.5, 276.25 425.75, 275.5 424.5, 275.5 423.5, 275.5 422.5, 275.75 421.5, 276.5 420.25, 277.75 419.75, 278.75 419.25, 279.75 419, 281.25 418.5, 282.75 418.5, 284.25 418.5, 285.25 418.5, 286.25 418.5, 287.25 418.5, 288.5 418.75, 289.5 419.25, 290.5 419.75, 291.5 420.75, 292 421.75, 292.75 423.25, 293.25 424.25, 293.25 425.25, 293.25 426.25, 292.75 427.25, 291.75 427.5, 290.75 427.75, 289.75 428, 288 429, 285 429), (272 413, 268 410, 267 403, 270 400, 276 398, 281 403, 281 409, 277 414, 272 413), (282 393, 279 387, 286 382, 294 385, 297 394, 292 398, 288 396, 282 393), (316 403, 310 397, 313 389, 322 387, 331 398, 323 402, 316 403), (306 422, 302 418, 305 411, 315 411, 323 420, 313 425, 306 422), (264 417, 265 417, 266 417, 267 417, 268 417, 269 417, 270 416.75, 271.25 416.5, 272.25 416.25, 273.25 416.25, 274.5 416, 275.5 415.75, 276.5 415.5, 277.5 415.25, 278.5 414.75, 279.75 414.25, 280.75 414, 281.75 413.75, 282.75 413.25, 283.75 412.5, 284.5 411.5, 285.25 410.75, 285.75 409.5, 286 408.5, 286 407.5, 286 406.5, 285.75 405.5, 285 404.5, 284.5 403.5, 284 402.5, 283.25 401.75, 282.25 401, 281.5 400, 280.5 399, 279.75 398, 279 397.25, 278.5 396.25, 277.75 395.25, 277 394.5, 276.25 393.75, 275.5 392.5, 274.75 391.75, 273.75 391.25, 273.25 390.25, 272.75 389.25, 272 388.25, 271.5 387.25, 270.75 386.5, 270.25 385.5, 269.5 384.75, 268.75 384, 268 383, 267.25 382, 266.5 381, 266 380, 265.75 379, 266.5 378.25, 267.5 378.25, 268.75 378.5, 270 379.25, 270.5 380.25, 271.25 381.25, 272.25 382.25, 272.75 383.25, 273.25 384.25, 274 385.25, 274.75 386.25, 275.25 387.25, 276 388.5, 276.75 389.25, 277.25 390.5, 278 391.25, 278.75 392, 279 393, 279.75 393.75, 280.25 394.75, 281.25 395.5, 282.25 396.5, 283 397.5, 284 398, 285 398.5, 286 399, 287 399.25, 288 399.75, 289 400, 290 400.5, 291 400.75, 292 401, 293 401, 294.25 401, 295.5 400.75, 296.75 400, 297.75 399.5, 298.5 398.75, 298.5 397.5, 299 396, 299.5 394.5, 299.75 393.5, 300 392.5, 300.25 391.25, 300.75 390.25, 301 389.25, 301.75 388, 302.25 387, 302.25 386, 303.25 385.25, 303.5 384.25, 304.5 384, 305.25 384.75, 305.25 386.25, 305.25 387.5, 305.25 388.5, 305.25 389.75, 305.25 390.75, 305 391.75, 304.5 392.75, 304.25 394.25, 304 395.25, 303.5 396.5, 303 397.5, 302.5 398.75, 302.25 399.75, 302.25 400.75, 303.5 401.5, 304.5 401.5, 305.75 401.5, 307.25 401.75, 308.25 402, 310.25 402.25, 311.75 402.75, 313 403, 314 403.25, 315.25 404, 316.75 404.5, 317.75 404.75, 318.75 405, 319.75 405.25, 320.75 405.5, 322 406, 323.5 406.25, 325 406.5, 326.5 406.75, 327.5 406.75, 329.25 407, 331 407.25, 332 407.5, 333 407.75, 334 408, 335.5 408.5, 336.75 408.75, 337.75 409.5, 338.5 410.25, 337 410.25, 336 410.25, 334.75 410.25, 333.75 410.25, 332.75 410.25, 331.75 410.25, 330.25 410, 329 409.75, 328 409.5, 327 409.25, 326 409.25, 325 409, 324 409, 322.75 408.5, 321.75 408.25, 320.75 408.25, 319.5 408.25, 318.5 408, 317.25 408, 316.25 408, 315 408, 314 408, 312.75 407.75, 311.75 407.75, 310.5 407.75, 309.5 407.5, 308.25 407.5, 307.25 407.5, 306 407.5, 305 407.5, 303.5 407.5, 302.25 407.5, 301 407.5, 299.75 407.5, 298.75 407.5, 297.75 407.5, 296.75 407.5, 295.75 407.5, 294.5 407.5, 293.5 407.5, 292.5 407.5, 292 408.5, 292 409.5, 292 410.75, 292 411.75, 292 412.75, 292.75 414, 293.75 414.75, 294.75 415.5, 296 416.25, 297.25 417.25, 299 418.25, 300.25 419.5, 301.25 420.25, 302 421, 303 422, 303.75 423, 304.5 424, 305.25 425, 306.25 426, 307.25 427, 308 428.25, 309 429, 309.25 430, 309.5 431, 308.5 431.5, 307.5 430.75, 306.25 430, 305.25 429, 304.5 428, 303.5 427, 302.75 426.25, 301.75 425.25, 301 424.5, 300.25 423.75, 299.5 423, 298.75 422.25, 297.75 421.75, 297 421, 296.25 420.25, 295.25 419.75, 294.25 419, 293.25 418.25, 292.25 417.5, 291.25 417, 290.25 416.75, 289.25 416.25, 288.25 416, 287.25 416, 286.25 416, 285.25 416, 284.25 416, 283.25 416, 282.25 416, 281.25 416.25, 280.25 416.5, 279.5 417.25, 278.5 417.75, 277.5 418.25, 276.5 418.5, 275.5 418.75, 274.5 419, 273.5 419, 272.5 419.25, 271.25 419.25, 270.25 419.75, 269.25 420, 268 420.5, 261 420, 264 417))";
        unionEqualsOrigin(poly);
    }

    public void testCrayCurveTwo() throws ParseException {
        String poly = "POLYGON ((7 144, 7 154, 8 164, 14 173, 16 162, 19 152, 25 142, 31 134, 37 126, 45 118, 54 112, 65 114, 74 119, 82 125, 85 135, 86 145, 86 157, 85 168, 83 180, 78 190, 76 201, 74 211, 74 221, 74 232, 74 244, 75 255, 76 269, 76 283, 77 294, 80 307, 85 316, 86 326, 82 336, 72 331, 64 325, 55 318, 51 308, 50 297, 50 275, 49 264, 48 250, 48 239, 49 225, 50 214, 52 194, 53 183, 56 172, 61 162, 67 149, 67 138, 57 131, 46 133, 38 141, 34 151, 32 162, 32 226, 31 237, 31 250, 31 263, 31 274, 31 285, 32 296, 33 306, 35 320, 40 329, 45 340, 52 352, 59 362, 73 368, 81 377, 90 387, 98 395, 108 402, 118 410, 129 416, 142 421, 155 421, 167 421, 180 421, 196 420, 213 420, 231 418, 241 417, 251 414, 267 409, 278 404, 290 400, 300 410, 310 407, 320 408, 330 409, 341 411, 352 412, 362 412, 373 412, 383 412, 394 412, 404 412, 415 410, 425 408, 433 400, 428 391, 421 383, 412 378, 402 375, 392 376, 382 378, 370 378, 360 379, 350 381, 340 384, 329 387, 318 387, 308 387, 297 386, 286 385, 275 383, 265 382, 253 380, 242 378, 231 375, 221 371, 211 366, 201 364, 190 361, 180 356, 172 349, 161 344, 158 332, 155 318, 155 306, 152 294, 151 283, 148 271, 147 261, 146 250, 146 228, 147 218, 149 206, 154 195, 158 185, 163 176, 171 167, 176 157, 182 149, 190 140, 197 130, 207 121, 217 116, 228 111, 238 109, 262 109, 272 110, 283 112, 293 115, 303 119, 312 124, 320 130, 328 138, 336 144, 344 150, 347 160, 344 170, 338 179, 328 183, 318 182, 308 179, 300 173, 290 168, 280 168, 269 168, 259 171, 249 174, 241 180, 234 188, 228 196, 222 204, 217 213, 217 224, 217 235, 218 247, 218 257, 221 268, 226 277, 234 283, 243 288, 252 293, 261 298, 271 303, 282 308, 292 310, 302 310, 313 310, 324 309, 335 307, 345 304, 355 302, 365 304, 363 314, 357 323, 348 331, 337 337, 326 340, 314 342, 304 343, 281 343, 268 342, 258 339, 247 336, 237 332, 227 327, 218 322, 209 316, 201 309, 193 300, 189 290, 188 279, 188 257, 189 247, 189 234, 190 223, 193 213, 196 202, 201 191, 208 181, 213 172, 220 163, 229 154, 238 147, 248 143, 269 143, 280 144, 290 147, 298 154, 308 159, 316 153, 312 143, 304 136, 293 132, 282 130, 272 130, 261 130, 249 130, 238 132, 226 136, 218 142, 209 150, 200 158, 193 167, 186 176, 179 184, 171 194, 167 204, 166 214, 166 227, 166 238, 167 250, 169 261, 171 275, 173 287, 175 297, 176 308, 178 319, 183 333, 189 341, 200 350, 212 358, 223 363, 235 368, 246 368, 259 369, 269 373, 279 374, 289 374, 300 372, 312 371, 323 367, 332 362, 342 358, 352 354, 364 349, 377 343, 387 338, 394 328, 399 317, 404 305, 406 293, 406 281, 406 231, 405 221, 403 209, 400 199, 395 183, 391 161, 385 137, 379 125, 373 116, 367 104, 360 96, 351 88, 342 83, 332 76, 319 70, 308 68, 295 63, 283 60, 271 57, 259 54, 245 52, 234 51, 220 51, 210 51, 197 50, 167 50, 152 51, 140 54, 126 58, 109 64, 94 69, 83 70, 73 73, 63 79, 53 84, 43 90, 35 98, 27 105, 20 113, 12 124, 9 134, 7 144), (37 188, 38 178, 38 167, 41 157, 50 150, 56 145, 40 220, 37 209, 37 188), (38 300, 40 290, 40 300, 45 306, 47 316, 53 324, 61 330, 67 338, 75 344, 83 350, 92 356, 100 364, 110 366, 108 346, 106 336, 103 326, 103 316, 101 306, 97 296, 97 286, 106 281, 110 291, 113 301, 114 311, 114 321, 117 332, 118 342, 119 352, 121 363, 121 373, 123 383, 130 391, 139 396, 149 399, 159 402, 169 404, 179 406, 189 407, 199 407, 209 407, 219 407, 228 412, 218 414, 166 414, 156 412, 144 410, 134 408, 125 403, 117 396, 109 388, 99 381, 89 374, 81 368, 73 362, 65 356, 58 348, 53 338, 48 329, 43 319, 38 310, 38 300), (46 105, 51 101, 60 96, 68 90, 78 91, 88 94, 99 99, 107 106, 112 115, 114 125, 118 135, 121 125, 122 114, 124 104, 127 94, 133 85, 138 76, 147 68, 156 63, 166 59, 196 59, 207 60, 217 62, 229 64, 239 66, 245 74, 234 74, 224 73, 214 70, 204 70, 194 70, 184 70, 174 71, 164 74, 153 78, 143 85, 135 94, 132 104, 131 114, 131 124, 131 135, 130 147, 128 159, 124 169, 122 179, 121 190, 119 200, 116 210, 116 221, 116 231, 116 241, 114 252, 105.5 252, 99 246, 99 236, 100 226, 108 206, 110 196, 110 186, 110 176, 110 166, 110 155, 110 145, 108 135, 105 125, 91 109, 81 107, 70 107, 60 110, 46 105), (72.5 112.5, 82.5 114, 90 121, 95 130, 99.5 139, 101.5 150, 101.5 160, 97.5 180, 94 190, 93 200.5, 92 211, 89.5 222, 89.5 232, 93 242, 97 251.5, 106 257, 116 259, 121.5 250, 121.5 240, 121.5 230, 124.5 220, 130 229, 129 239.5, 127 249.5, 123.5 260, 114.5 264.5, 104.5 265, 94.5 263, 84.5 260.5, 83.5 251, 87 196.5, 91 187, 93.5 176.5, 94.5 165.5, 96.5 154.5, 96 144, 94 134, 88.5 125, 81 118, 72.5 112.5), (80 340, 87 340, 87 338, 88 328, 92 337.5, 97.5 346, 102.5 348.5, 103.5 338, 106.5 348, 105 358, 96 353, 87.5 347, 80 340), (85 273, 100 340, 91 329, 90 320, 87 308, 87 297, 86 287, 85 273), (94 82, 103 76, 113 76, 123 76, 113 80, 110 90, 115 105, 110 96, 102 88, 94 82), (114 286, 140 290, 140 380, 114 286), (130 200, 133 189, 136 179, 141 170, 143 160, 143 149, 143 138, 144 128, 144 118, 146 108, 150 98, 156 90, 166 86, 177 85, 187 85, 197 84, 207 83, 228 83, 238 86, 229 91, 218 91, 208 93, 198 95, 188 98, 178 100, 168 103, 160 109, 155 118, 152 128, 152 138, 152 148, 151 158, 149 168, 146 178, 142 188, 139 198, 135 208, 130 210, 130 200), (175 256, 177 246, 177 235, 177 225, 177 215, 181 205, 184 195, 190 203, 187 213, 182 222, 180 232, 180 242, 180 252, 181 263, 182 273, 182 283, 185 293, 189 303, 194 312, 200 320, 207 328, 215 334, 223 340, 230 340, 320 360, 278 360, 268 361, 247 361, 237 358, 227 354, 218 348, 209 341, 201 335, 187 319, 186 308, 184 298, 180 288, 176 278, 176 267, 175 256), (225 242, 227 232, 234 224, 244 220, 253 215, 251 205, 256 196, 266 192, 277 190, 297 190, 307 193, 317 195, 327 195, 337 195, 347 194, 357 194, 366 189, 371 180, 371 160, 368 150, 362 142, 354 136, 347 128, 339 122, 329 117, 324 108, 334 110, 344 114, 353 120, 361 126, 368 134, 373 143, 380 151, 383 161, 383 171, 382 181, 378 191, 373 201, 364 207, 334 207, 324 205, 314 204, 304 201, 293 200, 283 200, 273 202, 264 207, 267 217, 277 220, 287 222, 297 222, 308 222, 318 222, 328 222, 338 222, 348 224, 358 225, 368 227, 379 229, 380 239, 370 239, 360 240, 340 238, 320 238, 310 237, 300 235, 290 234, 280 231, 270 229, 260 228, 250 228, 240 230, 232 236, 236 246, 245 251, 255 253, 265 255, 275 257, 286 261, 297 264, 307 265, 318 268, 329 268, 339 268, 350 270, 357 277, 347 280, 337 280, 307 274, 287 268, 277 268, 266 267, 256 266, 236 258, 227 253, 225 242), (240 240, 355 254, 400 250, 360 260, 240 240), (373 269, 384 264, 395 262, 400 290, 380 310, 380 300, 378 289, 374 279, 373 269))";
        unionEqualsOrigin(poly);
    }

    public void testRegularPolygon() throws ParseException {
        int length = 11;
        int numOfSide = 10;
        Coordinate[] coordinates = getRegularCoordinates(length, numOfSide);
        Polygon poly = createRegularPoly(coordinates);
        GeometryCollection expected = getExpectedRegularGeo(coordinates.clone());
        runCompare(poly, expected);
    }

    public void testTriangleWithHoles() throws ParseException {
        triangleWithHolesHelper(1);
        triangleWithHolesHelper(2);
        triangleWithHolesHelper(4);
    }

    public void triangleWithHolesHelper(int numOfHoles) throws ParseException {
        unionEqualsOrigin(getHoles(numOfHoles));
    }

    protected String getHoles(int numOfHoles) throws ParseException {
        String[] holeStr = new String[4];
        holeStr[0] = "POLYGON ((202 282, 113.5 133, 298 157, 302.5 175.5, 202 282), (202 282, 194 245, 215 245, 202 282))";
        // Two holes whose left most x on the same vertical line
        holeStr[1] = "POLYGON ((16 102, 346 409, 488 69, 359 24, 16 102), (250 250, 304 304, 326 254, 250 250), (250 150, 347 194, 380 144, 250 150))";
        holeStr[2] = "POLYGON ((250 380, 5 114, 480 70, 250 380), (200 260, 200 140, 250 200, 200 260), (140 200, 200 200, 200 150, 120 150, 140 200))";
        holeStr[3] = "POLYGON((-2 -1, 5 0, 0 6, -2 -1), (1 2, 0 1, 1 1, 1 2), (-1 0, 0 0, -1 1, -1 0), (-1 2, 0 2, 0 3, -1 2), (4 0, 4 1, 3 0, 4 0))";
        return holeStr[numOfHoles - 1];
    }

    /**
     * Check if there is overlap among earclipped triangles. Then union them
     * back to one polygon and compare with the original.
     * 
     * @param result EarClipped polygon
     * @param original Before applying EarClipper
     */
    protected void unionEqualsOrigin(Geometry result, Geometry original) {
        int size = result.getNumGeometries();
        Geometry union = result.getGeometryN(0);
        for (int i = 1; i < size; i++) {
            Geometry current = result.getGeometryN(i);
            if (!union.overlaps(current)) {
                union = union.union(current);
            }
        }
        original.normalize();
        union.normalize();
        assertTrue(original.equalsTopo(union));
    }

    protected void unionEqualsOrigin(String original) throws ParseException {
        Geometry geo = reader.read(original);
        Geometry result = runEarClip(geo);
        unionEqualsOrigin(result, geo.union());
    }

    protected GeometryCollection getExpectedRegularGeo(Coordinate[] coordinates) {
        int size = coordinates.length - 2 - 1;
        Polygon[] array = new Polygon[size];
        for (int i = 0; i < size; i++) {
            Coordinate[] tmp = { coordinates[0], coordinates[i + 1],
                    coordinates[i + 2], coordinates[0] };
            LinearRing ring = new GeometryFactory().createLinearRing(tmp);
            array[i] = new Polygon(ring, null, fact);
        }
        return new GeometryCollection(array, fact);
    }

    protected Polygon createRegularPoly(Coordinate[] coordinates) {
        LinearRing linear = new GeometryFactory().createLinearRing(coordinates);
        Polygon poly = new Polygon(linear, null, fact);
        poly.normalize();
        return poly;
    }

    /**
     * Create "random" regular polygon based on edge length and number of edges
     * 
     * @param length
     * @param numOfSide
     * @return
     */
    protected Coordinate[] getRegularCoordinates(int length, int numOfSide) {
        Coordinate[] ring = new Coordinate[numOfSide + 1];
        // always pass origin point for now
        int x = 0;
        int y = 0;
        // relative angle, depended on the shape
        double addAngle = Math.toRadians(180
                - ((double) (180 * (numOfSide - 2))) / numOfSide);
        ring[0] = new Coordinate(x, y);
        ring[1] = new Coordinate(x, y + length);
        ring[numOfSide] = new Coordinate(x, y);
        for (int i = 1; i < numOfSide - 1; i++) {
            double angle = getAngle(ring[i].x - ring[i - 1].x, ring[i].y
                    - ring[i - 1].y)
                    + addAngle;
            double newX = ring[i].x + length * Math.cos(angle);
            double newY = ring[i].y + length * Math.sin(angle);
            ring[i + 1] = new Coordinate(newX, newY);
        }
        return ring;
    }

    protected double getAngle(double x, double y) {
        if (x == 0 && y > 0)
            return Math.PI / 2;
        if (x == 0 && y < 0)
            return Math.PI * 3 / 2;
        double angle = Math.atan(y / x);
        if (x < 0)
            angle += Math.PI;
        return angle;
    }

    protected void runCompare(Geometry sitesGeo, Geometry expected) {
        Geometry result = runEarClip(sitesGeo);
        result.normalize();
        expected.normalize();
        assertTrue(expected.equalsExact(result, COMPARISON_TOLERANCE));
    }

    protected void runCompare(String sitesWKT, String expectedWKT)
            throws ParseException {
        Geometry sitesGeo = reader.read(sitesWKT);
        Geometry expected = reader.read(expectedWKT);
        runCompare(sitesGeo, expected);
    }

    protected Geometry runEarClip(Geometry g) {
        return runEarClip(g, false);
    }

    protected Geometry runEarClip(Geometry g, boolean improve) {
        // extract first polygon
        EarClipper clipper = new EarClipper((Polygon) g.getGeometryN(0));
        clipper.setImprove(improve);
        Geometry ears = clipper.getResult();
        return ears;
    }
}
