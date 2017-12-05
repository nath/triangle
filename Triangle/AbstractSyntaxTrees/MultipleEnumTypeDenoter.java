/*
 * @(#)MultipleFieldTypeDenoter.java                2.0 1999/08/11
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

public class MultipleEnumTypeDenoter extends EnumTypeDenoter {

    public MultipleEnumTypeDenoter(Identifier iAST, EnumTypeDenoter etAST,
                                   SourcePosition thePosition) {
        super(thePosition);
        I = iAST;
        ET = etAST;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitMultipleEnumTypeDenoter(this, o);
    }

    public boolean equals(Object obj) {
        if (obj != null && obj instanceof MultipleEnumTypeDenoter) {
            MultipleEnumTypeDenoter et = (MultipleEnumTypeDenoter) obj;
            return (this.I.spelling.compareTo(et.I.spelling) == 0) &&
                    this.ET.equals(et.ET);
        } else
            return obj == StdEnvironment.enumType;
    }

    public int getVal(Identifier iAST) {
        if (this.I.spelling.equals(iAST.spelling))
            return val;

        return this.ET.getVal(iAST);
    }

    public Identifier I;
    public int val;
    public EnumTypeDenoter ET;
}
