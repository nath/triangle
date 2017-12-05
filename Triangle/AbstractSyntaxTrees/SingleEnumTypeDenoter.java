/*
 * @(#)SingleFieldTypeDenoter.java                2.0 1999/08/11
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

import Triangle.StdEnvironment;
import Triangle.SyntacticAnalyzer.SourcePosition;

public class SingleEnumTypeDenoter extends EnumTypeDenoter {

    public SingleEnumTypeDenoter(Identifier iAST, SourcePosition thePosition) {
        super(thePosition);
        I = iAST;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitSingleEnumTypeDenoter(this, o);
    }

    public boolean equals(Object obj) {
        if (obj != null && obj instanceof SingleEnumTypeDenoter) {
            SingleEnumTypeDenoter ft = (SingleEnumTypeDenoter) obj;
            return (this.I.spelling.compareTo(ft.I.spelling) == 0);
        } else
            return obj == StdEnvironment.enumType;
    }

    @Override
    public int getVal(Identifier iAST) {
        if (this.I.spelling.equals(iAST.spelling))
            return val;

        return 9999; //shouldn't happen, checker should catch
    }

    public Identifier I;
    public int val;
}
