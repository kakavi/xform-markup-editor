package org.openxdata.markup.transformers

import groovy.transform.CompileStatic
import org.openxdata.markup.IFormElement
import org.openxdata.markup.IQuestion
import org.openxdata.markup.ISelectionQuestion
import org.openxdata.markup.TextQuestion

/**
 * Created by user on 6/30/2017.
 */
@CompileStatic
class FormBuilder {

    IFormElement elem

    static FormBuilder create() {
        new FormBuilder()
    }

    FormBuilder textQn() {
        elem = new TextQuestion()
        return this
    }


    FormBuilder text(String txt) {
        question.setText(txt)
        return this
    }

    FormBuilder binding(String binding) {
        elem.setId(binding)
        return this
    }

    FormBuilder calculation(String expr) {
        question.calculation = expr
        return this
    }

    FormBuilder showIf(String skipLogic) {
        addSkipLogic('show', skipLogic)
        return this
    }

    FormBuilder enableIf(String skipLogic) {
        addSkipLogic('enable', skipLogic)
        return this
    }

    FormBuilder disableIf(String skipLogic) {
        addSkipLogic('disable', skipLogic)
        return this
    }

    FormBuilder hideIf(String skipLogic) {
        addSkipLogic('hide', skipLogic)
        return this
    }


    FormBuilder addSkipLogic(String action, String expr) {
        question.skipLogic = expr
        question.skipAction = action
        return this
    }

    FormBuilder visible(boolean visible) {
        elem.visible = visible
        return this
    }

    FormBuilder meta(TransformAttribute attribute) {
        elem.line = attribute.line
        return this
    }

    FormBuilder line(int line) {
        elem.line = line
        return this
    }

    private IQuestion getQuestion() {
        (IQuestion) elem
    }

    IQuestion question() { (IQuestion) elem }

    ISelectionQuestion getSelectQuestion() {
        (ISelectionQuestion) elem
    }

    def <T> T getAs(T) { (T) elem }
}


