/*
 * @(#)Encoder.java                        2.0 1999/08/11
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

package Triangle.CodeGenerator;

import TAM.Instruction;
import TAM.Machine;
import Triangle.AbstractSyntaxTrees.*;
import Triangle.ErrorReporter;
import Triangle.StdEnvironment;

import javax.crypto.Mac;
import javax.lang.model.type.ArrayType;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class Encoder implements Visitor {


    // Commands
    public Object visitAssignCommand(AssignCommand ast, Object o) {
        Frame frame = (Frame) o;
        Integer valSize = (Integer) ast.E.visit(this, frame);
        encodeStore(ast.V, new Frame(frame, valSize), valSize);
        return null;
    }

    public Object visitCallCommand(CallCommand ast, Object o) {
        Frame frame = (Frame) o;
        Integer argsSize = (Integer) ast.APS.visit(this, frame);
        ast.I.visit(this, new Frame(frame.level, argsSize));
        return null;
    }

    public Object visitEmptyCommand(EmptyCommand ast, Object o) {
        return null;
    }

    public Object visitIfCommand(IfCommand ast, Object o) {
        Frame frame = (Frame) o;
        int jumpifAddr, jumpAddr;

        Integer valSize = (Integer) ast.E.visit(this, frame);
        jumpifAddr = nextInstrAddr;
        emit(Machine.JUMPIFop, Machine.falseRep, Machine.CBr, 0);
        ast.C1.visit(this, frame);
        jumpAddr = nextInstrAddr;
        emit(Machine.JUMPop, 0, Machine.CBr, 0);
        patch(jumpifAddr, nextInstrAddr);
        ast.C2.visit(this, frame);
        patch(jumpAddr, nextInstrAddr);
        return null;
    }

    public Object visitCaseCommand(CaseCommand ast, Object o) {
        Frame frame = (Frame) o;

        int extraSize = (Integer) ast.E.visit(this, frame);
        ast.CA.visit(this, frame);
        for (CaseAggregate curr = ast.CA; curr instanceof IntegerLiteralCaseAggregate; curr = ((IntegerLiteralCaseAggregate) curr).CA) {
            patch(((IntegerLiteralCaseAggregate) curr).jumpAddr, nextInstrAddr);
        }
        emit(Machine.POPop, 0, 0, 1);
        return null;
    }

    public Object visitForCommand(ForCommand ast, Object o) {
        Frame frame = (Frame) o;
        int jumpAddr, loopAddr;


        int extraSize = (Integer) ast.CD.visit(this, frame);
        loopAddr = nextInstrAddr;
        if (ast.CD.entity instanceof KnownValue) {
            extraSize = 1;
            emit(Machine.LOADLop, 0, 0, ((KnownValue) ast.CD.entity).value);
            ast.CD.entity = new UnknownValue(1, frame.level, frame.size);
        }
        emit(Machine.LOADop, extraSize, Machine.STr, -1);
        ast.E2.visit(this, frame);
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.geDisplacement);
        jumpAddr = nextInstrAddr;
        emit(Machine.JUMPIFop, Machine.trueRep, Machine.CBr, 0);
        ast.C.visit(this, new Frame(frame, extraSize));
        emit(Machine.LOADop, extraSize, frame.level, frame.size);
        emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.succDisplacement);
        emit(Machine.STOREop, extraSize, frame.level, frame.size);
        emit(Machine.JUMPop, 0, Machine.CBr, loopAddr);
        patch(jumpAddr, nextInstrAddr);
        if (extraSize > 0)
            emit(Machine.POPop, 0, 0, extraSize);
        return null;
    }

    public Object visitLetCommand(LetCommand ast, Object o) {
        Frame frame = (Frame) o;
        int extraSize = ((Integer) ast.D.visit(this, frame)).intValue();
        ast.C.visit(this, new Frame(frame, extraSize));
        if (extraSize > 0)
            emit(Machine.POPop, 0, 0, extraSize);
        return null;
    }

    public Object visitSequentialCommand(SequentialCommand ast, Object o) {
        ast.C1.visit(this, o);
        ast.C2.visit(this, o);
        return null;
    }

    public Object visitWhileCommand(WhileCommand ast, Object o) {
        Frame frame = (Frame) o;
        int jumpAddr, loopAddr;

        jumpAddr = nextInstrAddr;
        emit(Machine.JUMPop, 0, Machine.CBr, 0);
        loopAddr = nextInstrAddr;
        ast.C.visit(this, frame);
        patch(jumpAddr, nextInstrAddr);
        ast.E.visit(this, frame);
        emit(Machine.JUMPIFop, Machine.trueRep, Machine.CBr, loopAddr);
        return null;
    }

    public Object visitRepeatCommand(RepeatCommand ast, Object o) {
        Frame frame = (Frame) o;
        int loopAddr;

        loopAddr = nextInstrAddr;
        ast.C.visit(this, frame);
        ast.E.visit(this, frame);
        emit(Machine.JUMPIFop, Machine.falseRep, Machine.CBr, loopAddr);
        return null;
    }


    // Expressions
    public Object visitArrayExpression(ArrayExpression ast, Object o) {
        ast.type.visit(this, null);
        return ast.AA.visit(this, o);
    }

    public Object visitBinaryExpression(BinaryExpression ast, Object o) {
        Frame frame = (Frame) o;
        Integer valSize = (Integer) ast.type.visit(this, null);
        int valSize1 = ((Integer) ast.E1.visit(this, frame)).intValue();
        Frame frame1 = new Frame(frame, valSize1);
        int valSize2 = ((Integer) ast.E2.visit(this, frame1)).intValue();
        Frame frame2 = new Frame(frame.level, valSize1 + valSize2);
        if (ast.O.spelling.equals("<<"))
            emit(Machine.LOADLop, 0, 0, (Integer) ast.E1.type.visit(this, null));
        ast.O.visit(this, frame2);
        return valSize;
    }

    public Object visitCallExpression(CallExpression ast, Object o) {
        Frame frame = (Frame) o;
        Integer valSize = (Integer) ast.type.visit(this, null);
        Integer argsSize = (Integer) ast.APS.visit(this, frame);
        ast.I.visit(this, new Frame(frame.level, argsSize));
        return valSize;
    }

    public Object visitCharacterExpression(CharacterExpression ast,
                                           Object o) {
        Frame frame = (Frame) o;
        Integer valSize = (Integer) ast.type.visit(this, null);
        emit(Machine.LOADLop, 0, 0, ast.CL.spelling.charAt(1));
        return valSize;
    }

    public Object visitFixedStringExpression(FixedStringExpression ast, Object o) {
        Integer valSize = (Integer) ast.type.visit(this, null);
        ast.FSL.visit(this, null);
        return valSize;
    }

    public Object visitEmptyExpression(EmptyExpression ast, Object o) {
        return 0;
    }

    public Object visitIfExpression(IfExpression ast, Object o) {
        Frame frame = (Frame) o;
        Integer valSize;
        int jumpifAddr, jumpAddr;

        ast.type.visit(this, null);
        ast.E1.visit(this, frame);
        jumpifAddr = nextInstrAddr;
        emit(Machine.JUMPIFop, Machine.falseRep, Machine.CBr, 0);
        valSize = (Integer) ast.E2.visit(this, frame);
        jumpAddr = nextInstrAddr;
        emit(Machine.JUMPop, 0, Machine.CBr, 0);
        patch(jumpifAddr, nextInstrAddr);
        valSize = (Integer) ast.E3.visit(this, frame);
        patch(jumpAddr, nextInstrAddr);
        return valSize;
    }

    public Object visitIntegerExpression(IntegerExpression ast, Object o) {
        Frame frame = (Frame) o;
        Integer valSize = (Integer) ast.type.visit(this, null);
        emit(Machine.LOADLop, 0, 0, Integer.parseInt(ast.IL.spelling));
        return valSize;
    }

    public Object visitLetExpression(LetExpression ast, Object o) {
        Frame frame = (Frame) o;
        ast.type.visit(this, null);
        int extraSize = ((Integer) ast.D.visit(this, frame)).intValue();
        Frame frame1 = new Frame(frame, extraSize);
        Integer valSize = (Integer) ast.E.visit(this, frame1);
        if (extraSize > 0)
            emit(Machine.POPop, valSize.intValue(), 0, extraSize);
        return valSize;
    }

    public Object visitNilExpression(NilExpression ast, Object o) {
        Frame frame = (Frame) o;
        emit(Machine.LOADLop, 0, 0, 0);
        return 1;
    }

    public Object visitRecordExpression(RecordExpression ast, Object o) {
        ast.type.visit(this, null);
        Integer valSize = (Integer) ast.RA.visit(this, o);
        if (ast.type.recursive) {
            emit(Machine.LOADLop, 0, 0, valSize);
            emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.newDisplacement);
            emit(Machine.LOADop, valSize + 1, Machine.STr, -1 - valSize);
            emit(Machine.STOREIop, valSize, 0, 0);
            emit(Machine.POPop, 1, 0, valSize);
            return 1;
        }
        return valSize;
    }

    public Object visitUnaryExpression(UnaryExpression ast, Object o) {
        Frame frame = (Frame) o;
        Integer valSize = (Integer) ast.type.visit(this, null);
        ast.E.visit(this, frame);
        ast.O.visit(this, new Frame(frame.level, valSize));
        return valSize;
    }

    public Object visitVnameExpression(VnameExpression ast, Object o) {
        Frame frame = (Frame) o;
        Integer valSize = (Integer) ast.type.visit(this, null);
        encodeFetch(ast.V, frame, valSize.intValue());
        return valSize;
    }


    // Declarations
    public Object visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast,
                                                 Object o) {
        return new Integer(0);
    }

    public Object visitConstDeclaration(ConstDeclaration ast, Object o) {
        Frame frame = (Frame) o;
        int extraSize = 0;

        if (ast.E instanceof CharacterExpression) {
            CharacterLiteral CL = ((CharacterExpression) ast.E).CL;
            ast.entity = new KnownValue(Machine.characterSize,
                    characterValuation(CL.spelling));
        } else if (ast.E instanceof IntegerExpression) {
            IntegerLiteral IL = ((IntegerExpression) ast.E).IL;
            ast.entity = new KnownValue(Machine.integerSize,
                    Integer.parseInt(IL.spelling));
        } else {
            int valSize = ((Integer) ast.E.visit(this, frame)).intValue();
            ast.entity = new UnknownValue(valSize, frame.level, frame.size);
            extraSize = valSize;
        }
        writeTableDetails(ast);
        return new Integer(extraSize);
    }

    public Object visitFuncDeclaration(FuncDeclaration ast, Object o) {
        Frame frame = (Frame) o;
        int jumpAddr = nextInstrAddr;
        int argsSize = 0, valSize = 0;

        emit(Machine.JUMPop, 0, Machine.CBr, 0);
        ast.entity = new KnownRoutine(Machine.closureSize, frame.level, nextInstrAddr);
        writeTableDetails(ast);
        if (frame.level == Machine.maxRoutineLevel)
            reporter.reportRestriction("can't nest routines more than 7 deep");
        else {
            Frame frame1 = new Frame(frame.level + 1, 0);
            argsSize = (Integer) ast.FPS.visit(this, frame1);
            Frame frame2 = new Frame(frame.level + 1, Machine.linkDataSize);
            valSize = (Integer) ast.E.visit(this, frame2);

            ast.FPS.copyResults(this, frame2);
        }
        emit(Machine.RETURNop, valSize, 0, argsSize);
        patch(jumpAddr, nextInstrAddr);
        return 0;
    }

    public Object visitOpFuncDeclaration(OpFuncDeclaration ast, Object o) {
        Frame frame = (Frame) o;
        int jumpAddr = nextInstrAddr;
        int argsSize = 0, valSize = 0;

        emit(Machine.JUMPop, 0, Machine.CBr, 0);
        ast.entity = new KnownRoutine(Machine.closureSize, frame.level, nextInstrAddr);
        writeTableDetails(ast);
        if (frame.level == Machine.maxRoutineLevel)
            reporter.reportRestriction("can't nest routines more than 7 deep");
        else {
            Frame frame1 = new Frame(frame.level + 1, 0);
            argsSize = (Integer) ast.FPS.visit(this, frame1);
            Frame frame2 = new Frame(frame.level + 1, Machine.linkDataSize);
            valSize = (Integer) ast.E.visit(this, frame2);
        }
        emit(Machine.RETURNop, valSize, 0, argsSize);
        patch(jumpAddr, nextInstrAddr);
        return 0;
    }

    public Object visitProcDeclaration(ProcDeclaration ast, Object o) {
        Frame frame = (Frame) o;
        int jumpAddr = nextInstrAddr;
        int argsSize = 0;

        emit(Machine.JUMPop, 0, Machine.CBr, 0);
        ast.entity = new KnownRoutine(Machine.closureSize, frame.level,
                nextInstrAddr);
        writeTableDetails(ast);
        if (frame.level == Machine.maxRoutineLevel)
            reporter.reportRestriction("can't nest routines so deeply");
        else {
            Frame frame1 = new Frame(frame.level + 1, 0);
            argsSize = ((Integer) ast.FPS.visit(this, frame1)).intValue();
            Frame frame2 = new Frame(frame.level + 1, Machine.linkDataSize);
            ast.C.visit(this, frame2);

            ast.FPS.copyResults(this, frame2);
        }
        emit(Machine.RETURNop, 0, 0, argsSize);
        patch(jumpAddr, nextInstrAddr);
        return 0;
    }

    public Object visitSequentialDeclaration(SequentialDeclaration ast, Object o) {
        Frame frame = (Frame) o;
        int extraSize1, extraSize2;

        extraSize1 = ((Integer) ast.D1.visit(this, frame)).intValue();
        Frame frame1 = new Frame(frame, extraSize1);
        extraSize2 = ((Integer) ast.D2.visit(this, frame1)).intValue();
        return new Integer(extraSize1 + extraSize2);
    }

    public Object visitTypeDeclaration(TypeDeclaration ast, Object o) {
        // just to ensure the type's representation is decided
        ast.T.visit(this, null);
        return 0;
    }

    public Object visitRecTypeDeclaration(RecTypeDeclaration ast, Object o) {
        // just to ensure the type's representation is decided
        //ast.T.visit(this, null);
        return 0;
    }

    public Object visitEnumTypeDeclaration(EnumTypeDeclaration ast, Object o) {
        return 0;
    }

    public Object visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast,
                                                Object o) {
        return new Integer(0);
    }

    public Object visitVarDeclaration(VarDeclaration ast, Object o) {
        Frame frame = (Frame) o;
        int extraSize;

        extraSize = ((Integer) ast.T.visit(this, null)).intValue();
        emit(Machine.PUSHop, 0, 0, extraSize);
        ast.entity = new KnownAddress(Machine.addressSize, frame.level, frame.size);
        if (ast.T instanceof ArrayTypeDenoter)
            ((KnownAddress) ast.entity).arrLen = extraSize / ((ArrayTypeDenoter) ast.T).T.entity.size;
        if (ast.T instanceof FixedStringTypeDenoter)
            ((KnownAddress) ast.entity).arrLen = Integer.parseInt(((FixedStringTypeDenoter) ast.T).IL.spelling);
        writeTableDetails(ast);
        return new Integer(extraSize);
    }

    public Object visitVarInitialization(VarInitialization ast, Object o) {
        Frame frame = (Frame) o;
        int extraSize;

        extraSize = (Integer) ast.T.visit(this, null);
        ast.E.visit(this, frame);
        ast.entity = new KnownAddress(Machine.addressSize, frame.level, frame.size);
        writeTableDetails(ast);
        return new Integer(extraSize);
    }

    public Object visitPackageDeclaration(PackageDeclaration ast, Object o) {
        int valSize1, valSize2 = 0;

        if (ast.D2 != null)
            valSize2 = (Integer) ast.D2.visit(this, o);
        valSize1 = (Integer) ast.D1.visit(this, o);

        return valSize1 + valSize2;
    }

    // Array Aggregates
    public Object visitMultipleArrayAggregate(MultipleArrayAggregate ast,
                                              Object o) {
        Frame frame = (Frame) o;
        int elemSize = ((Integer) ast.E.visit(this, frame)).intValue();
        Frame frame1 = new Frame(frame, elemSize);
        int arraySize = ((Integer) ast.AA.visit(this, frame1)).intValue();
        return new Integer(elemSize + arraySize);
    }

    public Object visitSingleArrayAggregate(SingleArrayAggregate ast, Object o) {
        return ast.E.visit(this, o);
    }


    // Record Aggregates
    public Object visitMultipleRecordAggregate(MultipleRecordAggregate ast,
                                               Object o) {
        Frame frame = (Frame) o;
        int fieldSize = ((Integer) ast.E.visit(this, frame)).intValue();
        Frame frame1 = new Frame(frame, fieldSize);
        int recordSize = ((Integer) ast.RA.visit(this, frame1)).intValue();
        return new Integer(fieldSize + recordSize);
    }

    public Object visitSingleRecordAggregate(SingleRecordAggregate ast,
                                             Object o) {
        return ast.E.visit(this, o);
    }


    // Case Aggregates
    public Object visitIntegerLiteralCaseAggregate(IntegerLiteralCaseAggregate ast,
                                                   Object o) {
        Frame frame = (Frame) o;
        int jumpIfAddr, jumpPast;

        emit(Machine.LOADop, 1, Machine.STr, -1);
        jumpIfAddr = nextInstrAddr;
        emit(Machine.JUMPIFop, Integer.parseInt(ast.IL.spelling), Machine.CBr, 0);
        jumpPast = nextInstrAddr;
        emit(Machine.JUMPop, 0, Machine.CBr, 0);
        patch(jumpIfAddr, nextInstrAddr);
        ast.C.visit(this, frame);
        ast.jumpAddr = nextInstrAddr;
        emit(Machine.JUMPop, 0, Machine.CBr, 0);
        patch(jumpPast, nextInstrAddr);
        ast.CA.visit(this, frame);
        return null;
    }

    public Object visitElseCaseAggregate(ElseCaseAggregate ast,
                                         Object o) {
        return ast.C.visit(this, o);
    }


    // Formal Parameters
    public Object visitConstFormalParameter(ConstFormalParameter ast, Object o) {
        Frame frame = (Frame) o;
        int valSize = ((Integer) ast.T.visit(this, null)).intValue();
        ast.entity = new UnknownValue(valSize, frame.level, -frame.size - valSize);
        writeTableDetails(ast);
        return new Integer(valSize);
    }

    public Object visitFuncFormalParameter(FuncFormalParameter ast, Object o) {
        Frame frame = (Frame) o;
        int argsSize = Machine.closureSize;
        ast.entity = new UnknownRoutine(Machine.closureSize, frame.level,
                -frame.size - argsSize);
        writeTableDetails(ast);
        return new Integer(argsSize);
    }

    public Object visitProcFormalParameter(ProcFormalParameter ast, Object o) {
        Frame frame = (Frame) o;
        int argsSize = Machine.closureSize;
        ast.entity = new UnknownRoutine(Machine.closureSize, frame.level,
                -frame.size - argsSize);
        writeTableDetails(ast);
        return new Integer(argsSize);
    }

    public Object visitVarFormalParameter(VarFormalParameter ast, Object o) {
        Frame frame = (Frame) o;
//  int valSize = ((Integer) ast.T.visit(this, null)).intValue();
        int valSize = 1;    // [2003.03.15 ruys] should be the size of an absolute address
        ast.entity = new UnknownAddress(Machine.addressSize, frame.level,
                -frame.size - valSize);
        writeTableDetails(ast);
        return new Integer(Machine.addressSize);
    }

    public Object visitValResFormalParameter(ValResFormalParameter ast, Object o) {
        Frame frame = (Frame) o;
        int valSize = (Integer) ast.T.visit(this, null);
        ast.entity = new UnknownValue(valSize, frame.level, -frame.size - valSize - Machine.addressSize);
        writeTableDetails(ast);
        return valSize + Machine.addressSize;
    }
    public Object visitResFormalParameter(ResFormalParameter ast, Object o) {
        Frame frame = (Frame) o;
        int valSize = (Integer) ast.T.visit(this, null);
        ast.entity = new UnknownValue(valSize, frame.level, -frame.size - valSize - Machine.addressSize);
        writeTableDetails(ast);
        return valSize + Machine.addressSize;
    }

    public Object visitEmptyFormalParameterSequence(
            EmptyFormalParameterSequence ast, Object o) {
        return new Integer(0);
    }

    public Object visitMultipleFormalParameterSequence(
            MultipleFormalParameterSequence ast, Object o) {
        Frame frame = (Frame) o;
        int argsSize1 = ((Integer) ast.FPS.visit(this, frame)).intValue();
        Frame frame1 = new Frame(frame, argsSize1);
        int argsSize2 = ((Integer) ast.FP.visit(this, frame1)).intValue();
        return new Integer(argsSize1 + argsSize2);
    }

    public Object visitSingleFormalParameterSequence(
            SingleFormalParameterSequence ast, Object o) {
        return ast.FP.visit(this, o);
    }


    // Actual Parameters
    public Object visitConstActualParameter(ConstActualParameter ast, Object o) {
        return ast.E.visit(this, o);
    }

    public Object visitFuncActualParameter(FuncActualParameter ast, Object o) {
        Frame frame = (Frame) o;
        if (ast.I.decl.entity instanceof KnownRoutine) {
            ObjectAddress address = ((KnownRoutine) ast.I.decl.entity).address;
            // static link, code address
            emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level), 0);
            emit(Machine.LOADAop, 0, Machine.CBr, address.displacement);
        } else if (ast.I.decl.entity instanceof UnknownRoutine) {
            ObjectAddress address = ((UnknownRoutine) ast.I.decl.entity).address;
            emit(Machine.LOADop, Machine.closureSize, displayRegister(frame.level,
                    address.level), address.displacement);
        } else if (ast.I.decl.entity instanceof PrimitiveRoutine) {
            int displacement = ((PrimitiveRoutine) ast.I.decl.entity).displacement;
            // static link, code address
            emit(Machine.LOADAop, 0, Machine.SBr, 0);
            emit(Machine.LOADAop, 0, Machine.PBr, displacement);
        }
        return new Integer(Machine.closureSize);
    }

    public Object visitProcActualParameter(ProcActualParameter ast, Object o) {
        Frame frame = (Frame) o;
        if (ast.I.decl.entity instanceof KnownRoutine) {
            ObjectAddress address = ((KnownRoutine) ast.I.decl.entity).address;
            // static link, code address
            emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level), 0);
            emit(Machine.LOADAop, 0, Machine.CBr, address.displacement);
        } else if (ast.I.decl.entity instanceof UnknownRoutine) {
            ObjectAddress address = ((UnknownRoutine) ast.I.decl.entity).address;
            emit(Machine.LOADop, Machine.closureSize, displayRegister(frame.level,
                    address.level), address.displacement);
        } else if (ast.I.decl.entity instanceof PrimitiveRoutine) {
            int displacement = ((PrimitiveRoutine) ast.I.decl.entity).displacement;
            // static link, code address
            emit(Machine.LOADAop, 0, Machine.SBr, 0);
            emit(Machine.LOADAop, 0, Machine.PBr, displacement);
        }
        return new Integer(Machine.closureSize);
    }

    public Object visitVarActualParameter(VarActualParameter ast, Object o) {
        encodeFetchAddress(ast.V, (Frame) o);
        return new Integer(Machine.addressSize);
    }

    public Object visitValResActualParameter(ValResActualParameter ast, Object o) {
        Frame frame = (Frame) o;
        int valSize = (Integer) ast.V.type.visit(this, null);
        encodeFetch(ast.V, frame, valSize);
        encodeFetchAddress(ast.V, (Frame) o);
        return new Integer(Machine.addressSize + valSize);
    }

    public Object visitResActualParameter(ResActualParameter ast, Object o) {
        Frame frame = (Frame) o;
        int valSize = (Integer) ast.V.type.visit(this, null);
        emit(Machine.PUSHop, 0, 0 , valSize);
        encodeFetchAddress(ast.V, (Frame) o);
        return new Integer(Machine.addressSize + valSize);
    }

    public Object visitEmptyActualParameterSequence(
            EmptyActualParameterSequence ast, Object o) {
        return new Integer(0);
    }

    public Object visitMultipleActualParameterSequence(
            MultipleActualParameterSequence ast, Object o) {
        Frame frame = (Frame) o;
        int argsSize1 = ((Integer) ast.AP.visit(this, frame)).intValue();
        Frame frame1 = new Frame(frame, argsSize1);
        int argsSize2 = ((Integer) ast.APS.visit(this, frame1)).intValue();
        return new Integer(argsSize1 + argsSize2);
    }

    public Object visitSingleActualParameterSequence(
            SingleActualParameterSequence ast, Object o) {
        return ast.AP.visit(this, o);
    }


    // Type Denoters
    public Object visitAnyTypeDenoter(AnyTypeDenoter ast, Object o) {
        return new Integer(0);
    }

    public Object visitArrayTypeDenoter(ArrayTypeDenoter ast, Object o) {
        int typeSize;
        if (ast.entity == null) {
            int elemSize = ((Integer) ast.T.visit(this, null)).intValue();
            typeSize = Integer.parseInt(ast.IL.spelling) * elemSize;
            ast.entity = new TypeRepresentation(typeSize);
            writeTableDetails(ast);
        } else
            typeSize = ast.entity.size;
        return new Integer(typeSize);
    }

    public Object visitBoolTypeDenoter(BoolTypeDenoter ast, Object o) {
        if (ast.entity == null) {
            ast.entity = new TypeRepresentation(Machine.booleanSize);
            writeTableDetails(ast);
        }
        return new Integer(Machine.booleanSize);
    }

    public Object visitCharTypeDenoter(CharTypeDenoter ast, Object o) {
        if (ast.entity == null) {
            ast.entity = new TypeRepresentation(Machine.characterSize);
            writeTableDetails(ast);
        }
        return new Integer(Machine.characterSize);
    }

    public Object visitErrorTypeDenoter(ErrorTypeDenoter ast, Object o) {
        return new Integer(0);
    }

    public Object visitEnumTypeDenoter(EnumTypeDenoter ast, Object o) {
        return Machine.integerSize;
    }

    public Object visitFixedStringTypeDenoter(FixedStringTypeDenoter ast, Object o) {
        return Integer.parseInt(ast.IL.spelling) * Machine.characterSize;
    }

    public Object visitNilTypeDenoter(NilTypeDenoter ast, Object o) {
        return new Integer(1);
    }

    public Object visitSimpleTypeDenoter(SimpleTypeDenoter ast,
                                         Object o) {
        return new Integer(0);
    }

    public Object visitIntTypeDenoter(IntTypeDenoter ast, Object o) {
        if (ast.entity == null) {
            ast.entity = new TypeRepresentation(Machine.integerSize);
            writeTableDetails(ast);
        }
        return new Integer(Machine.integerSize);
    }

    public Object visitRecordTypeDenoter(RecordTypeDenoter ast, Object o) {
        int typeSize;

        if (ast.recursive) {
            if (ast.entity == null) {
                ast.entity = new UnknownAddress(0, 0, 0); //dummy
                typeSize = (Integer) ast.FT.visit(this, 0);
                ast.entity = new UnknownAddress(typeSize, 0, 0);
                writeTableDetails(ast);
            }
            return 1;
        }
        else if (ast.entity == null) {
            typeSize = ((Integer) ast.FT.visit(this, new Integer(0))).intValue();
            ast.recursive = ast.FT.recursive;
            ast.entity = new TypeRepresentation(typeSize);
            writeTableDetails(ast);
        } else
            typeSize = ast.entity.size;
        return new Integer(typeSize);
    }


    public Object visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast,
                                                Object o) {
        int offset = ((Integer) o).intValue();
        int fieldSize;

        if (ast.entity == null) {
            fieldSize = ((Integer) ast.T.visit(this, null)).intValue();
            ast.entity = new Field(fieldSize, offset);
            writeTableDetails(ast);
        } else
            fieldSize = ast.entity.size;

        Integer offset1 = new Integer(offset + fieldSize);
        int recSize = ((Integer) ast.FT.visit(this, offset1)).intValue();
        ast.recursive = ast.T.recursive || ast.T == StdEnvironment.nilType || ast.FT.recursive;
        return new Integer(fieldSize + recSize);
    }

    public Object visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast,
                                              Object o) {
        int offset = ((Integer) o).intValue();
        int fieldSize;

        if (ast.entity == null) {
            fieldSize = ((Integer) ast.T.visit(this, null)).intValue();
            ast.entity = new Field(fieldSize, offset);
            writeTableDetails(ast);
        } else
            fieldSize = ast.entity.size;

        ast.recursive = ast.T.recursive || ast.T == StdEnvironment.nilType;

        return new Integer(fieldSize);
    }

    //public Object visitEnumTypeDenoter

    public Object visitMultipleEnumTypeDenoter(MultipleEnumTypeDenoter ast, Object o) {
        return Machine.integerSize;
    }

    public Object visitSingleEnumTypeDenoter(SingleEnumTypeDenoter ast, Object o) {
        return Machine.integerSize;
    }

    // Literals, Identifiers and Operators
    public Object visitCharacterLiteral(CharacterLiteral ast, Object o) {
        return null;
    }

    public Object visitFixedStringLiteral(FixedStringLiteral ast, Object o) {
        Frame frame = (Frame) o;

        for (Character c : ast.spelling.toCharArray()) {
            emit(Machine.LOADLop, 0, 0, c);
        }

        return null;
    }

    public Object visitIdentifier(Identifier ast, Object o) {
        Frame frame = (Frame) o;
        if (ast.decl.entity instanceof KnownRoutine) {
            ObjectAddress address = ((KnownRoutine) ast.decl.entity).address;
            emit(Machine.CALLop, displayRegister(frame.level, address.level),
                    Machine.CBr, address.displacement);
        } else if (ast.decl.entity instanceof UnknownRoutine) {
            ObjectAddress address = ((UnknownRoutine) ast.decl.entity).address;
            emit(Machine.LOADop, Machine.closureSize, displayRegister(frame.level,
                    address.level), address.displacement);
            emit(Machine.CALLIop, 0, 0, 0);
        } else if (ast.decl.entity instanceof PrimitiveRoutine) {
            int displacement = ((PrimitiveRoutine) ast.decl.entity).displacement;
            if (displacement != Machine.idDisplacement)
                emit(Machine.CALLop, Machine.SBr, Machine.PBr, displacement);
        } else if (ast.decl.entity instanceof EqualityRoutine) { // "=" or "\="
            int displacement = ((EqualityRoutine) ast.decl.entity).displacement;
            emit(Machine.LOADLop, 0, 0, frame.size / 2);
            emit(Machine.CALLop, Machine.SBr, Machine.PBr, displacement);
        }
        return null;
    }

    public Object visitPackagedIdentifier(PackagedIdentifier ast, Object o) {
        return ast.I.visit(this, o);
    }

    public Object visitIntegerLiteral(IntegerLiteral ast, Object o) {
        return null;
    }

    public Object visitOperator(Operator ast, Object o) {
        Frame frame = (Frame) o;
        if (ast.decl.entity instanceof KnownRoutine) {
            ObjectAddress address = ((KnownRoutine) ast.decl.entity).address;
            emit(Machine.CALLop, displayRegister(frame.level, address.level),
                    Machine.CBr, address.displacement);
        } else if (ast.decl.entity instanceof UnknownRoutine) {
            ObjectAddress address = ((UnknownRoutine) ast.decl.entity).address;
            emit(Machine.LOADop, Machine.closureSize, displayRegister(frame.level,
                    address.level), address.displacement);
            emit(Machine.CALLIop, 0, 0, 0);
        } else if (ast.decl.entity instanceof PrimitiveRoutine) {
            int displacement = ((PrimitiveRoutine) ast.decl.entity).displacement;
            if (displacement != Machine.idDisplacement)
                emit(Machine.CALLop, Machine.SBr, Machine.PBr, displacement);
        } else if (ast.decl.entity instanceof EqualityRoutine) { // "=" or "\="
            int displacement = ((EqualityRoutine) ast.decl.entity).displacement;
            emit(Machine.LOADLop, 0, 0, frame.size / 2);
            emit(Machine.CALLop, Machine.SBr, Machine.PBr, displacement);
        }
        return null;
    }


    // Value-or-variable names
    public Object visitDotVname(DotVname ast, Object o) {
        Frame frame = (Frame) o;
        RuntimeEntity baseObject = (RuntimeEntity) ast.V.visit(this, frame);
        ast.offset =  ((Field) ast.I.decl.entity).fieldOffset;
        if (!ast.V.type.recursive)
            ast.offset += ast.V.offset;
        // I.decl points to the appropriate record field
        ast.indexed = ast.V.indexed;
        return baseObject;
    }

    public Object visitSimpleVname(SimpleVname ast, Object o) {
        ast.offset = 0;
        ast.indexed = false;

        return ast.I.decl.entity;
    }

    public Object visitSubscriptVname(SubscriptVname ast, Object o) {
        Frame frame = (Frame) o;
        RuntimeEntity baseObject;
        int elemSize, indexSize;

        baseObject = (RuntimeEntity) ast.V.visit(this, frame);
        int arrLen = baseObject instanceof KnownAddress ? ((KnownAddress) baseObject).arrLen : ((UnknownValue) baseObject).size;
        ast.offset = ast.V.offset;
        ast.indexed = ast.V.indexed;
        elemSize = (Integer) ast.type.visit(this, null);
        if (ast.E instanceof IntegerExpression) {
            IntegerLiteral IL = ((IntegerExpression) ast.E).IL;
            ast.offset = ast.offset + Integer.parseInt(IL.spelling) * elemSize;
            emit(Machine.LOADLop, 0, 0, Integer.parseInt(IL.spelling));
            emit(Machine.LOADLop, 0, 0, 0);
            emit(Machine.LOADLop, 0, 0, arrLen);
            emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.rangecheckDisplacement);
            emit(Machine.POPop, 0, 0, 1);
        } else {
            // v-name is indexed by a proper expression, not a literal
            if (ast.indexed)
                frame.size = frame.size + Machine.integerSize;
            indexSize = ((Integer) ast.E.visit(this, frame)).intValue();
            if (elemSize != 1) {
                emit(Machine.LOADLop, 0, 0, elemSize);
                emit(Machine.CALLop, Machine.SBr, Machine.PBr,
                        Machine.multDisplacement);
            }
            if (ast.indexed)
                emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
            else
                ast.indexed = true;

            emit(Machine.LOADLop, 0, 0, 0);
            emit(Machine.LOADLop, 0, 0, arrLen);
            emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.rangecheckDisplacement);
        }
        return baseObject;
    }


    // Programs
    public Object visitProgram(Program ast, Object o) {
        return ast.C.visit(this, o);
    }

    public Encoder(ErrorReporter reporter) {
        this.reporter = reporter;
        nextInstrAddr = Machine.CB;
        elaborateStdEnvironment();
    }

    private ErrorReporter reporter;

    // Generates code to run a program.
    // showingTable is true iff entity description details
    // are to be displayed.
    public final void encodeRun(Program theAST, boolean showingTable) {
        tableDetailsReqd = showingTable;
        //startCodeGeneration();
        theAST.visit(this, new Frame(0, 0));
        emit(Machine.HALTop, 0, 0, 0);
    }

    // Decides run-time representation of a standard constant.
    private final void elaborateStdConst(Declaration constDeclaration,
                                         int value) {

        if (constDeclaration instanceof ConstDeclaration) {
            ConstDeclaration decl = (ConstDeclaration) constDeclaration;
            int typeSize = ((Integer) decl.E.type.visit(this, null)).intValue();
            decl.entity = new KnownValue(typeSize, value);
            writeTableDetails(constDeclaration);
        }
    }

    // Decides run-time representation of a standard routine.
    private final void elaborateStdPrimRoutine(Declaration routineDeclaration,
                                               int routineOffset) {
        routineDeclaration.entity = new PrimitiveRoutine(Machine.closureSize, routineOffset);
        writeTableDetails(routineDeclaration);
    }

    private final void elaborateStdEqRoutine(Declaration routineDeclaration,
                                             int routineOffset) {
        routineDeclaration.entity = new EqualityRoutine(Machine.closureSize, routineOffset);
        writeTableDetails(routineDeclaration);
    }

    private final void elaborateStdRoutine(Declaration routineDeclaration,
                                           int routineOffset) {
        routineDeclaration.entity = new KnownRoutine(Machine.closureSize, 0, routineOffset);
        writeTableDetails(routineDeclaration);
    }

    private final void elaborateStdEnvironment() {
        tableDetailsReqd = false;
        elaborateStdConst(StdEnvironment.falseDecl, Machine.falseRep);
        elaborateStdConst(StdEnvironment.trueDecl, Machine.trueRep);
        elaborateStdPrimRoutine(StdEnvironment.notDecl, Machine.notDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.andDecl, Machine.andDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.orDecl, Machine.orDisplacement);
        elaborateStdConst(StdEnvironment.maxintDecl, Machine.maxintRep);
        elaborateStdPrimRoutine(StdEnvironment.addDecl, Machine.addDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.subtractDecl, Machine.subDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.multiplyDecl, Machine.multDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.divideDecl, Machine.divDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.moduloDecl, Machine.modDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.lessDecl, Machine.ltDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.notgreaterDecl, Machine.leDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.greaterDecl, Machine.gtDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.notlessDecl, Machine.geDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.fixedLexDecl, Machine.fixedLexDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.chrDecl, Machine.idDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.ordDecl, Machine.idDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.eolDecl, Machine.eolDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.eofDecl, Machine.eofDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.getDecl, Machine.getDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.putDecl, Machine.putDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.getintDecl, Machine.getintDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.putintDecl, Machine.putintDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.geteolDecl, Machine.geteolDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.puteolDecl, Machine.puteolDisplacement);
        elaborateStdEqRoutine(StdEnvironment.equalDecl, Machine.eqDisplacement);
        elaborateStdEqRoutine(StdEnvironment.unequalDecl, Machine.neDisplacement);

        elaborateStdPrimRoutine(StdEnvironment.succDecl, Machine.succDisplacement);
        elaborateStdPrimRoutine(StdEnvironment.predDecl, Machine.predDisplacement);
    }

    // Saves the object program in the named file.

    public void saveObjectProgram(String objectName) {
        FileOutputStream objectFile = null;
        DataOutputStream objectStream = null;

        int addr;

        try {
            objectFile = new FileOutputStream(objectName);
            objectStream = new DataOutputStream(objectFile);

            addr = Machine.CB;
            for (addr = Machine.CB; addr < nextInstrAddr; addr++)
                Machine.code[addr].write(objectStream);
            objectFile.close();
        } catch (FileNotFoundException s) {
            System.err.println("Error opening object file: " + s);
        } catch (IOException s) {
            System.err.println("Error writing object file: " + s);
        }
    }

    boolean tableDetailsReqd;

    public static void writeTableDetails(AST ast) {
    }

    // OBJECT CODE

    // Implementation notes:
    // Object code is generated directly into the TAM Code Store, starting at CB.
    // The address of the next instruction is held in nextInstrAddr.

    private int nextInstrAddr;

    // Appends an instruction, with the given fields, to the object code.
    public void emit(int op, int n, int r, int d) {
        Instruction nextInstr = new Instruction();
        if (n > 255) {
            reporter.reportRestriction("length of operand can't exceed 255 words");
            n = 255; // to allow code generation to continue
        }
        nextInstr.op = op;
        nextInstr.n = n;
        nextInstr.r = r;
        nextInstr.d = d;
        if (nextInstrAddr == Machine.PB)
            reporter.reportRestriction("too many instructions for code segment");
        else {
            Machine.code[nextInstrAddr] = nextInstr;
            nextInstrAddr = nextInstrAddr + 1;
        }
    }

    // Patches the d-field of the instruction at address addr.
    private void patch(int addr, int d) {
        Machine.code[addr].d = d;
    }

    // DATA REPRESENTATION

    public int characterValuation(String spelling) {
        // Returns the machine representation of the given character literal.
        return spelling.charAt(1);
        // since the character literal is of the form 'x'}
    }

    // REGISTERS

    // Returns the register number appropriate for object code at currentLevel
    // to address a data object at objectLevel.
    public int displayRegister(int currentLevel, int objectLevel) {
        if (objectLevel == 0)
            return Machine.SBr;
        else if (currentLevel - objectLevel <= 6)
            return Machine.LBr + currentLevel - objectLevel; // LBr|L1r|...|L6r
        else {
            reporter.reportRestriction("can't access data more than 6 levels out");
            return Machine.L6r;  // to allow code generation to continue
        }
    }

    private Boolean isResultParam(Vname V) {
        if (V instanceof SimpleVname) {
            SimpleVname SV = (SimpleVname) V;
            return SV.I.decl instanceof ResFormalParameter || SV.I.decl instanceof ValResFormalParameter;
        }

        if (V instanceof DotVname) {
            return isResultParam(V);
        }

        return false;
    }

    // Generates code to store the value of a named constant or variable
    // and push it on to the stack.
    // currentLevel is the routine level where the vname occurs.
    // frameSize is the anticipated size of the local stack frame when
    // the constant or variable is fetched at run-time.
    // valSize is the size of the constant or variable's value.

    private void encodeStore(Vname V, Frame frame, int valSize) {
        RuntimeEntity baseObject = (RuntimeEntity) V.visit(this, frame);

        if (V.type.recursive) valSize = 1;

        // If indexed = true, code will have been generated to load an index value.
        if (valSize > 255) {
            reporter.reportRestriction("can't store values larger than 255 words");
            valSize = 255; // to allow code generation to continue
        }
        if (baseObject instanceof KnownAddress) {
            ObjectAddress address = ((KnownAddress) baseObject).address;
            if (V.indexed) {
                emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level),
                        address.displacement + V.offset);
                emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
                emit(Machine.STOREIop, valSize, 0, 0);
            } else {
                emit(Machine.STOREop, valSize, displayRegister(frame.level,
                        address.level), address.displacement + V.offset);
            }
        } else if (baseObject instanceof UnknownAddress) {
            ObjectAddress address = ((UnknownAddress) baseObject).address;
            emit(Machine.LOADop, Machine.addressSize, displayRegister(frame.level,
                    address.level), address.displacement);
            if (V.indexed)
                emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
            if (V.offset != 0) {
                emit(Machine.LOADLop, 0, 0, V.offset);
                emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
            }
            emit(Machine.STOREIop, valSize, 0, 0);
        } else if (isResultParam(V)) {
            ObjectAddress address = ((UnknownValue) baseObject).address;
            emit(Machine.STOREop, Machine.addressSize, displayRegister(frame.level, address.level), address.displacement);
        }
    }

    // Generates code to fetch the value of a named constant or variable
    // and push it on to the stack.
    // currentLevel is the routine level where the vname occurs.
    // frameSize is the anticipated size of the local stack frame when
    // the constant or variable is fetched at run-time.
    // valSize is the size of the constant or variable's value.

    private void encodeFetch(Vname V, Frame frame, int valSize) {
        RuntimeEntity baseObject = (RuntimeEntity) V.visit(this, frame);

        if (V instanceof DotVname && ((DotVname) V).V.type.recursive) {
            DotVname DV = (DotVname) V;
            encodeFetch(DV.V, new Frame(frame, 1), 1);
            emit(Machine.LOADLop, 0, 0, V.offset);
            emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
            emit(Machine.LOADIop, valSize, 0, 0);
            return;
        }

        // load enum literals onto stack
        if (V.type instanceof EnumTypeDenoter && ((SimpleVname) V).I.decl instanceof EnumTypeDeclaration) {
            emit(Machine.LOADLop, 0, 0, ((EnumTypeDeclaration) ((SimpleVname) V).I.decl).getVal(((SimpleVname) V).I));
            return;
        }

        // If indexed = true, code will have been generated to load an index value.
        if (valSize > 255) {
            reporter.reportRestriction("can't load values larger than 255 words");
            valSize = 255; // to allow code generation to continue
        }
        if (baseObject instanceof KnownValue) {
            // presumably offset = 0 and indexed = false
            int value = ((KnownValue) baseObject).value;
            emit(Machine.LOADLop, 0, 0, value);
        } else if ((baseObject instanceof UnknownValue) ||
                (baseObject instanceof KnownAddress)) {
            ObjectAddress address = (baseObject instanceof UnknownValue) ?
                    ((UnknownValue) baseObject).address :
                    ((KnownAddress) baseObject).address;
            if (V.indexed) {
                emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level),
                        address.displacement + V.offset);
                emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
                emit(Machine.LOADIop, valSize, 0, 0);
            } else
                emit(Machine.LOADop, valSize, displayRegister(frame.level,
                        address.level), address.displacement + V.offset);
        } else if (baseObject instanceof UnknownAddress) {
            ObjectAddress address = ((UnknownAddress) baseObject).address;
            emit(Machine.LOADop, Machine.addressSize, displayRegister(frame.level,
                    address.level), address.displacement);
            if (V.indexed)
                emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
            if (V.offset != 0) {
                emit(Machine.LOADLop, 0, 0, V.offset);
                emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
            }
            emit(Machine.LOADIop, valSize, 0, 0);
        }
    }

    // Generates code to compute and push the address of a named variable.
    // vname is the program phrase that names this variable.
    // currentLevel is the routine level where the vname occurs.
    // frameSize is the anticipated size of the local stack frame when
    // the variable is addressed at run-time.

    private void encodeFetchAddress(Vname V, Frame frame) {

        RuntimeEntity baseObject = (RuntimeEntity) V.visit(this, frame);
        // If indexed = true, code will have been generated to load an index value.
        if (baseObject instanceof KnownAddress) {
            ObjectAddress address = ((KnownAddress) baseObject).address;
            emit(Machine.LOADAop, 0, displayRegister(frame.level, address.level),
                    address.displacement + V.offset);
            if (V.indexed)
                emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
        } else if (baseObject instanceof UnknownAddress) {
            ObjectAddress address = ((UnknownAddress) baseObject).address;
            emit(Machine.LOADop, Machine.addressSize, displayRegister(frame.level,
                    address.level), address.displacement);
            if (V.indexed)
                emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
            if (V.offset != 0) {
                emit(Machine.LOADLop, 0, 0, V.offset);
                emit(Machine.CALLop, Machine.SBr, Machine.PBr, Machine.addDisplacement);
            }
        }
    }
}
