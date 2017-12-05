/*
 * @(#)ConstFormalParameter.java                        2.0 1999/08/11
 *
 * Copyright (C) 1999 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package Triangle.AbstractSyntaxTrees;

import TAM.Machine;
import Triangle.CodeGenerator.Encoder;
import Triangle.CodeGenerator.Frame;
import Triangle.CodeGenerator.UnknownValue;
import Triangle.SyntacticAnalyzer.SourcePosition;

public class ValResFormalParameter extends FormalParameter {

    public ValResFormalParameter(Identifier iAST, TypeDenoter tAST,
                                 SourcePosition thePosition) {
        super(thePosition);
        I = iAST;
        T = tAST;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitValResFormalParameter(this, o);
    }

    public void storeResult(Encoder e, Frame frame) {
        UnknownValue UV = (UnknownValue) this.entity;
        e.emit(Machine.LOADop, UV.size + Machine.addressSize, e.displayRegister(frame.level, UV.address.level), UV.address.displacement);
        e.emit(Machine.STOREIop, UV.size, 0, 0);
    }

    public Identifier I;
    public TypeDenoter T;
}
