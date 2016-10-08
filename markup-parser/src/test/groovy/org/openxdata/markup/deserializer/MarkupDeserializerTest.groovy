package org.openxdata.markup.deserializer

import org.openxdata.markup.HasQuestions
import org.openxdata.markup.exception.ValidationException
import org.openxdata.markup.serializer.XFormSerializer

import static org.openxdata.markup.deserializer.DeSerializerFixtures.nestedGroups

/**
 * Created by kay on 5/26/2016.
 */
class MarkupDeserializerTest extends GroovyTestCase {


    void testParse() {

        def erraticForm = """### fkfkf

## fjfjf
uu
   '''sdsdsd
ONew

sdsdsd''"""
        def ser = new MarkupDeserializer(erraticForm)


        shouldFail(RuntimeException) {
            ser.parse()
        }

    }

    void testSerializingNestedGroupsNumbered() {
        def f = new MarkupDeserializer(nestedGroups.markUp).study().forms.first()
        assert f['oldpageid'].line == 5
        assert f['group_page_1'].line == 11
        assert f['group_page_2'].line == 16
        assert f['oldpageid'].absoluteBinding == '/s_f_v1/oldpageid'
        assert f['group_page_2'].absoluteBinding == '/s_f_v1/oldpageid/group_page_1/group_page_2'

        assert f.allFirstLevelQuestions.size() == 1
        assert (f['group_page_1'] as HasQuestions).allFirstLevelQuestions.size() == 2
        assert (f['oldpageid'] as HasQuestions).allFirstLevelQuestions.size() == 3


        def xform = new XFormSerializer(numberBindings: true, numberQuestions: true).toXForm(f)
        assertEquals nestedGroups.xmlNumbered, xform

    }

    void testSerializingNestedGroups() {
        def f = new MarkupDeserializer(nestedGroups.markUp).study().forms.first()
        def xform = new XFormSerializer(numberBindings: false, numberQuestions: false).toXForm(f)
        assertEquals nestedGroups.xml, xform

    }


    void testGroupsWithNoIdsButHaveBindAttrs() {
        def form = '''### s
                     |## f
                     |@invisible
                     |#> sjdjsd
                     |sdsd
                     |@layout:appearance jjs
                     |#> sdsd
                     |sddsd'''.stripMargin()

        try {
            new MarkupDeserializer(form).study()
            fail("Expecting validation exception")
        } catch (ValidationException x) {
            assert x.message.contains('Has No Id')
        }
    }

    void testThatClosestElementIsFetched() {
        def form = '''### s
                     |## f
                     |
                     |#> sjdjsd
                     |kskas
                     |sdsd
                     |@layout:appearance jjs
                     |
                     |#> sdsd
                     |sddsd
                     |
                     |## f2
                     |
                     |sdj
                     |
                     |ddff
                     |>dsd
                     |
                     '''.stripMargin()

        def study = new MarkupDeserializer(form).parse()

        assert 'sjdjsd' == study.getElementClosestToLine(3).name
        assert 'sjdjsd' == study.getElementClosestToLine(4).name
        assert 'sdsd' == study.getElementClosestToLine(5).name
        assert 'ddff' == study.getElementClosestToLine(100).name
        assert 'f' == study.getElementClosestToLine(-1).name
        assert 'sdj' == study.getElementClosestToLine(14).name


    }
}
