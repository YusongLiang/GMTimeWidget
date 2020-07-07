package com.gome.gmtimewidget.util;

import android.graphics.Path;
import android.graphics.PointF;

import java.text.ParseException;

/**
 * @author Felix.Liang
 */
public class SvgPathParser {

    private static final int TOKEN_ABSOLUTE_COMMAND = 1;
    private static final int TOKEN_RELATIVE_COMMAND = 2;
    private static final int TOKEN_VALUE = 3;
    private static final int TOKEN_EOF = 4;

    private static PointF[] sPoints = new PointF[3];
    private static int sCurrentToken;
    private static PointF sCurrentPoint = new PointF();
    private static int sLength;
    private static int sIndex;
    private static String sPathString;

    static {
        for (int i = 0; i < sPoints.length; i++) {
            sPoints[i] = new PointF();
        }
    }

    private static float transformX(float x) {
        return x;
    }

    private static float transformY(float y) {
        return y;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static Path parsePath(String s) throws ParseException {
        sCurrentPoint.set(0, 0);
        sCurrentPoint.set(Float.NaN, Float.NaN);
        sPathString = s;
        sIndex = 0;
        sLength = sPathString.length();
        for (int i = 0; i < sPoints.length; i++) {
            sPoints[i].set(0, 0);
        }
        final PointF tempPoint1 = sPoints[0];
        final PointF tempPoint2 = sPoints[1];
        final PointF tempPoint3 = sPoints[2];
        Path p = new Path();
        p.setFillType(Path.FillType.WINDING);
        boolean firstMove = true;
        while (sIndex < sLength) {
            char command = consumeCommand();
            boolean relative = (sCurrentToken == TOKEN_RELATIVE_COMMAND);
            switch (command) {
                case 'M':
                case 'm': {
                    // move command
                    boolean firstPoint = true;
                    while (advanceToNextToken() == TOKEN_VALUE) {
                        consumeAndTransformPoint(tempPoint1, relative && sCurrentPoint.x != Float.NaN);
                        if (firstPoint) {
                            p.moveTo(tempPoint1.x, tempPoint1.y);
                            firstPoint = false;
                            if (firstMove) {
                                sCurrentPoint.set(tempPoint1);
                                firstMove = false;
                            }
                        } else {
                            p.lineTo(tempPoint1.x, tempPoint1.y);
                        }
                    }
                    sCurrentPoint.set(tempPoint1);
                    break;
                }
                case 'C':
                case 'c': {
                    // curve command
                    if (sCurrentPoint.x == Float.NaN) {
                        throw new ParseException("Relative commands require current point", sIndex);
                    }
                    while (advanceToNextToken() == TOKEN_VALUE) {
                        consumeAndTransformPoint(tempPoint1, relative);
                        consumeAndTransformPoint(tempPoint2, relative);
                        consumeAndTransformPoint(tempPoint3, relative);
                    }
                    p.cubicTo(tempPoint1.x, tempPoint1.y, tempPoint2.x, tempPoint2.y, tempPoint3.x,
                            tempPoint3.y);
                    sCurrentPoint.set(tempPoint3);
                    break;
                }

                case 'S':
                case 's': {
                    //smooth curve command
                    if (sCurrentPoint.x == Float.NaN) {
                        throw new ParseException("Relative commands require current point", sIndex);
                    }
                    while (advanceToNextToken() == TOKEN_VALUE) {
                        consumeAndTransformPoint(tempPoint1, relative);
                        consumeAndTransformPoint(tempPoint2, relative);
                    }
                    float a = (tempPoint2.x - sCurrentPoint.x) / (sCurrentPoint.y - tempPoint2.y);
                    float b = -1;
                    float c = (sCurrentPoint.y + tempPoint2.y) / 2
                            + (sCurrentPoint.x - tempPoint2.x) / (sCurrentPoint.y - tempPoint2.y) * (sCurrentPoint.x + tempPoint2.x) / 2;
                    tempPoint3.x = tempPoint1.x - 2 * a * (a * tempPoint1.x + b * tempPoint1.y + c) / (a * a + b * b);
                    tempPoint3.y = tempPoint1.y - 2 * b * (a * tempPoint1.x + b * tempPoint1.y + c) / (a * a + b * b);
                    p.cubicTo(tempPoint3.x, tempPoint3.y, tempPoint1.x, tempPoint1.y, tempPoint2.x, tempPoint2.y);
                    sCurrentPoint.set(tempPoint2);
                    break;
                }
                case 'L':
                case 'l': {
                    // line command
                    if (sCurrentPoint.x == Float.NaN) {
                        throw new ParseException("Relative commands require current point", sIndex);
                    }

                    while (advanceToNextToken() == TOKEN_VALUE) {
                        consumeAndTransformPoint(tempPoint1, relative);
                        p.lineTo(tempPoint1.x, tempPoint1.y);
                    }
                    sCurrentPoint.set(tempPoint1);
                    break;
                }
                case 'H':
                case 'h': {
                    // horizontal line command
                    if (sCurrentPoint.x == Float.NaN) {
                        throw new ParseException("Relative commands require current point", sIndex);
                    }

                    while (advanceToNextToken() == TOKEN_VALUE) {
                        float x = transformX(consumeValue());
                        if (relative) {
                            x += sCurrentPoint.x;
                        }
                        tempPoint1.x = x;
                        tempPoint1.y = sCurrentPoint.y;
                        p.lineTo(tempPoint1.x, tempPoint1.y);
                    }
                    sCurrentPoint.set(tempPoint1);
                    break;
                }
                case 'V':
                case 'v': {
                    // vertical line command
                    if (sCurrentPoint.x == Float.NaN) {
                        throw new ParseException("Relative commands require current point", sIndex);
                    }

                    while (advanceToNextToken() == TOKEN_VALUE) {
                        float y = transformY(consumeValue());
                        if (relative) {
                            y += sCurrentPoint.y;
                        }
                        tempPoint1.x = sCurrentPoint.x;
                        tempPoint1.y = y;
                        p.lineTo(tempPoint1.x, tempPoint1.y);
                    }
                    sCurrentPoint.set(tempPoint1);
                    break;
                }
                case 'Z':
                case 'z': {
                    // close command
                    p.close();
                    break;
                }
            }
        }
        return p;
    }

    private static int advanceToNextToken() {
        while (sIndex < sLength) {
            char c = sPathString.charAt(sIndex);
            if ('a' <= c && c <= 'z') {
                return (sCurrentToken = TOKEN_RELATIVE_COMMAND);
            } else if ('A' <= c && c <= 'Z') {
                return (sCurrentToken = TOKEN_ABSOLUTE_COMMAND);
            } else if (('0' <= c && c <= '9') || c == '.' || c == '-') {
                return (sCurrentToken = TOKEN_VALUE);
            }

            // skip unrecognized character
            ++sIndex;
        }

        return (sCurrentToken = TOKEN_EOF);
    }

    private static char consumeCommand() throws ParseException {
        advanceToNextToken();
        if (sCurrentToken != TOKEN_RELATIVE_COMMAND && sCurrentToken != TOKEN_ABSOLUTE_COMMAND) {

            throw new ParseException("Expected command", sIndex);
        }

        return sPathString.charAt(sIndex++);
    }

    private static void consumeAndTransformPoint(PointF out, boolean relative) throws ParseException {
        out.x = transformX(consumeValue());
        out.y = transformY(consumeValue());
        if (relative) {
            out.x += sCurrentPoint.x;
            out.y += sCurrentPoint.y;
        }
    }

    private static float consumeValue() throws ParseException {
        advanceToNextToken();
        if (sCurrentToken != TOKEN_VALUE) {
            throw new ParseException("Expected value", sIndex);
        }
        boolean start = true;
        boolean seenDot = false;
        int index = sIndex;
        while (index < sLength) {
            char c = sPathString.charAt(index);
            if (!('0' <= c && c <= '9') && (c != '.' || seenDot) && (c != '-' || !start)) {
                // end of value
                break;
            }
            if (c == '.') {
                seenDot = true;
            }
            start = false;
            ++index;
        }
        if (index == sIndex) {
            throw new ParseException("Expected value", sIndex);
        }
        String str = sPathString.substring(sIndex, index);
        try {
            float value = Float.parseFloat(str);
            sIndex = index;
            return value;
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid float value '" + str + "'.", sIndex);
        }
    }
}
