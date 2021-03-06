package org.openxdata.markup

import com.sun.org.apache.xerces.internal.util.XMLChar
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import org.openxdata.markup.exception.InvalidAttributeException
import org.openxdata.markup.exception.ValidationException

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 11/26/12
 * Time: 1:35 PM
 * To change this template use File | Settings | File Templates.
 */
class Util {


    public static final Integer CACHE_SIZE = 50

    @SuppressWarnings("UnnecessaryQualifiedReference")
    @CompileStatic
    @Memoized(protectedCacheSize = Util.CACHE_SIZE, maxCacheSize = Util.CACHE_SIZE)
    static String getBindName(String question) {
        // if len(s) < 1, return '_blank'
        if (question == null || question.length() < 1)
            return "_blank"

        def s = getTextWithoutDecTemplate(question)
        // return s.trim().replaceAll(/\s+/, "_").replaceAll(/\W/, "").toLowerCase()

        // Converts a string into a valid XML token (tag name)
        // No spaces, start with a letter or underscore, not 'xml*'

        // xml tokens must start with a letter
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_"

        // after the leading letter, xml tokens may have
        // digits, period, or hyphen
        String nameChars = letters + "0123456789.-"

        // special characters that should be replaced with valid text
        // all other invalid characters will be removed
        HashMap<String, String> swapChars = new HashMap<String, String>()
        swapChars.put("!", "bang")
        swapChars.put("#", "pound")
        swapChars.put("\\*", "star")
        swapChars.put("'", "apos")
        swapChars.put("\"", "quote")
        swapChars.put("%", "percent")
        swapChars.put("<", "lt")
        swapChars.put(">", "gt")
        swapChars.put("=", "eq")
        swapChars.put("/", "slash")
        swapChars.put("\\\\", "backslash")
        swapChars.put("\\.", "dot")
        swapChars.put("-", "hyphen")

        s = s.replace("'", "")

        // start by cleaning whitespace and converting to lowercase
        s = s.replaceAll("^\\s+", "").replaceAll(/\s+$/, "").replaceAll("\\s+", "_").toLowerCase()

        // swap characters
        Set<Map.Entry<String, String>> swaps = swapChars.entrySet()
        for (Map.Entry<String, String> entry : swaps) {
            if (entry.getValue() != null)
                s = s.replaceAll(entry.getKey(), "_" + entry.getValue() + "_")
            else
                s = s.replaceAll(String.valueOf(entry.getKey()), "")
        }

        // ensure that invalid characters and consecutive underscores are
        // removed
        String token = ""
        boolean underscoreFlag = false
        for (int i = 0; i < s.length(); i++) {
            if (nameChars.indexOf((int) s.charAt(i)) != -1) {
                if (s.charAt(i) != '_' || !underscoreFlag) {
                    token += s.charAt(i)
                    underscoreFlag = (s.charAt(i) == '_')
                }
            }
        }

        // remove extraneous underscores before returning token
        token = token.replaceAll("_+", "_")
        token = token.replaceAll(/_+$/, "")

        if(token.isEmpty()) return '_empty'

        // make sure token starts with valid letter
        try {
            if (letters.indexOf((int) token.charAt(0)) == -1 || token.startsWith("xml"))
                token = "_" + token
        } catch (Exception e) {
            e.printStackTrace()
            throw e
        }
        // return token
        return token
    }

    @CompileStatic
    static String getTextWithoutDecTemplate(String text) {
        if (text.contains('${')) {
            if (text.indexOf('}$') < text.length() - 2)
                text = text.substring(0, text.indexOf('${')) + text.substring(text.indexOf('}$') + 2)
            else
                text = text.substring(0, text.indexOf('${'))
        }
        return text
    }

    @CompileStatic
    static void writeToFile(String fileName, String contents) {
        File file = new File(fileName)
        while (file.exists()) {
            println "Deleting file $file.absolutePath"
            try {
                file.delete()
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
        file.setText(contents, 'UTF-8')

    }

    def static time(String name = "", Closure worker) {
        println "BenchmarkStart: $name"
        def start = System.currentTimeMillis()
        def rt = worker.call()
        def stop = System.currentTimeMillis()
        def time = stop - start
        def readableTime = TimeFormat.valueOf(time, TimeFormat.ROUND_TO_MILLISECOND)
        println "Completed in ${readableTime}"
        return [value: rt, time: time]
    }

    /**
     * This was used to try and guess datatype for a specific project I was working on
     * it is far from accurate. Just left it here just in case
     * @param bind
     * @return
     */
    static String getType(String bind) {
        boolean isBool = booleanKeys.any { bind.startsWith(it + '_') }
        if (isBool)
            return "boolean"

        boolean isDate = dates.any { bind.startsWith(it + '_') }
        if (isDate)
            return "date"

        return "string"
    }


    static replaceFirst(String self, String searchString, String replacement) {
        replace(self, searchString, replacement, 1)
    }

    public static final int INDEX_NOT_FOUND = -1
    //copied from apache commons
    @CompileStatic
    static String replace(final String text, final String searchString, final String replacement, int max) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text
        }
        int start = 0
        int end = text.indexOf(searchString, start)
        if (end == INDEX_NOT_FOUND) {
            return text
        }
        final int replLength = searchString.length()
        int increase = replacement.length() - replLength
        increase = increase < 0 ? 0 : increase
        increase *= max < 0 ? 16 : max > 64 ? 64 : max
        final StringBuilder buf = new StringBuilder(text.length() + increase)
        while (end != INDEX_NOT_FOUND) {
            buf.append(text.substring(start, end)).append(replacement)
            start = end + replLength
            if (--max == 0) {
                break
            }
            end = text.indexOf(searchString, start)
        }
        buf.append(text.substring(start))
        return buf.toString()
    }

    @CompileStatic
    static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0
    }

    static Map<String, String> parseBind(String option, int line) {
        try {
            parseBindStatic(option)
        } catch (ValidationException ex) {
            ex.line = line
            throw ex
        }

    }

    @CompileStatic
    @Memoized(maxCacheSize = Util.CACHE_SIZE, protectedCacheSize = Util.CACHE_SIZE)
    private static Map<String, String> parseBindStatic(String option) {
        String bind

        if (!option) {
            bind = getBindName(option)
        } else if (option[0] == '$') {
            def tmpBind = option.find(/[$][^\s]*\s/)?.trim()
            //make sure bind is at the beginning
            if (!Study.validateWithXML.get())
                validateId(tmpBind?.replaceFirst(/\$/, ''), 0)
            if (tmpBind == null || option.indexOf(tmpBind) > 0)
                throw new ValidationException("""Option [$option] has an invalid id.
                                                 |An Id should start with lower case characters follow by low case characters, numbers or underscores""".stripMargin().toString())
            option = option.replaceFirst(/[$][^\s]*/, '').trim()
            bind = tmpBind.trim() - '$'
        } else {
            bind = getBindName(option)
        }
        return [option: option, bind: bind]
    }

    @CompileStatic
    static void validateId(String id, int line) {
        validateId(id, line, false)
    }

    @CompileStatic
    static void validateId(String id, int line, boolean useXml) {
        try {
            if (Study.validateWithXML.get() || useXml)
                memoizedValidateGeneral.call(id)
            else
                memoizedValidateId.call(id)
        } catch (ValidationException ex) {
            ex.line = line
            throw ex
        }
    }


    private static Closure memoizedValidateId = { String id ->
        if (!(id ==~ /[a-z_][a-z0-9_]*/))
            throw new InvalidAttributeException("You have an invalid variable [$id] .\n" +
                    "Attributes should start with a small letter followed by small letters and underscores")
    }.memoizeBetween(CACHE_SIZE, CACHE_SIZE)

    private static Closure memoizedValidateGeneral = { String id ->
        if (!(XMLChar.isValidName(id)))
            throw new InvalidAttributeException("You have an invalid variable [$id] .\n" +
                    "Attributes should start with a small letter followed by small letters and underscores")
    }.memoizeBetween(CACHE_SIZE, CACHE_SIZE)


    def static booleanKeys = """is
                        has
                        do
                        any
                        does
                        are
                   """.split(/\s+/)

    def static dates = """period
                          date
                       """.split(/\s+/)


}
