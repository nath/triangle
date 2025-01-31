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

public class MultipleFieldTypeDenoter extends FieldTypeDenoter {

    public MultipleFieldTypeDenoter(Identifier iAST, TypeDenoter tAST, FieldTypeDenoter ftAST,
                                    SourcePosition thePosition) {
        super(thePosition);
        I = iAST;
        T = tAST;
        FT = ftAST;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitMultipleFieldTypeDenoter(this, o);
    }

    public boolean equals(Object obj) {
        if (obj != null && obj instanceof MultipleFieldTypeDenoter) {
            MultipleFieldTypeDenoter ft = (MultipleFieldTypeDenoter) obj;
            return (this.I.spelling.compareTo(ft.I.spelling) == 0) &&
                    ((this.T.recursive  && ft.T.recursive) || ft.T == StdEnvironment.nilType || this.T == StdEnvironment.nilType || this.T.equals(ft.T)) &&
                    this.FT.equals(ft.FT);
        } else
            return false;
    }

    public Identifier I;
    public TypeDenoter T;
    public FieldTypeDenoter FT;
}
