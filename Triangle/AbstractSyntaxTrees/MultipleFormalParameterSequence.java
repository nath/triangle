/*
 * @(#)MultipleFormalParameterSequence.java        2.0 1999/08/11
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

import Triangle.CodeGenerator.Encoder;
import Triangle.CodeGenerator.Frame;
import Triangle.SyntacticAnalyzer.SourcePosition;

public class MultipleFormalParameterSequence extends FormalParameterSequence {

    public MultipleFormalParameterSequence(FormalParameter fpAST, FormalParameterSequence fpsAST,
                                           SourcePosition thePosition) {
        super(thePosition);
        FP = fpAST;
        FPS = fpsAST;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitMultipleFormalParameterSequence(this, o);
    }

    public boolean equals(FormalParameterSequence fpsAST) {
        return true;
    }

    public void copyResults(Encoder e, Frame frame) {
        if (FP instanceof ResFormalParameter) {
            ((ResFormalParameter) FP).storeResult(e, frame);
        }
        if (FP instanceof ValResFormalParameter) {
            ((ValResFormalParameter) FP).storeResult(e, frame);
        }
        this.FPS.copyResults(e, frame);
    }

    public FormalParameter FP;
    public FormalParameterSequence FPS;
}
