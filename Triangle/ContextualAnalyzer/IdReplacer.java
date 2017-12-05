package Triangle.ContextualAnalyzer;

import Triangle.AbstractSyntaxTrees.*;

import java.util.HashMap;
import java.util.UUID;

public class IdReplacer implements Visitor {
    public Object visitAssignCommand(AssignCommand ast, Object o) {
        ast.V.visit(this, o);
        ast.E.visit(this, o);
        return null;
    }

    public Object visitCallCommand(CallCommand ast, Object o) {
        ast.I.visit(this, o);
        ast.APS.visit(this, o);
        return null;
    }

    public Object visitCaseCommand(CaseCommand ast, Object o) {
        ast.E.visit(this, o);
        ast.CA.visit(this, o);
        return null;
    }

    public Object visitEmptyCommand(EmptyCommand ast, Object o) {
        return null;
    }

    public Object visitForCommand(ForCommand ast, Object o) {
        ast.I.visit(this, o);
        ast.E1.visit(this, o);
        ast.E2.visit(this, o);
        return null;
    }

    public Object visitIfCommand(IfCommand ast, Object o) {
        ast.E.visit(this, o);
        ast.C1.visit(this, o);
        ast.C2.visit(this, o);
        return null;
    }

    public Object visitLetCommand(LetCommand ast, Object o) {
        ast.D.visit(this, o);
        ast.C.visit(this, o);
        return null;
    }

    public Object visitSequentialCommand(SequentialCommand ast, Object o) {
        ast.C1.visit(this, o);
        ast.C2.visit(this, o);
        return null;
    }

    public Object visitWhileCommand(WhileCommand ast, Object o) {
        ast.E.visit(this, o);
        ast.C.visit(this, o);
        return null;
    }

    public Object visitRepeatCommand(RepeatCommand ast, Object o) {
        ast.E.visit(this, o);
        ast.C.visit(this, o);
        return null;
    }

    public Object visitArrayExpression(ArrayExpression ast, Object o) {
        if (ast.invariant) {
            String uuid = UUID.randomUUID().toString();
            ((HashMap<String, Expression>) o).put(uuid, ast);
            ast.movedId = new VnameExpression(new SimpleVname(new Identifier(uuid, ast.position), ast.position), ast.position);
            ast.movedId.type = ast.type;
            return null;
        }
        ast.AA.visit(this, o);
        return null;
    }

    public Object visitBinaryExpression(BinaryExpression ast, Object o) {
        if (ast.invariant) {
            String uuid = UUID.randomUUID().toString();
            ((HashMap<String, Expression>) o).put(uuid, ast);
            ast.movedId = new VnameExpression(new SimpleVname(new Identifier(uuid, ast.position), ast.position), ast.position);
            ast.movedId.type = ast.type;
            return null;
        }
        ast.O.visit(this, o);
        ast.E1.visit(this, o);
        ast.E2.visit(this, o);
        return null;
    }

    public Object visitCallExpression(CallExpression ast, Object o) {
        ast.APS.visit(this, o);
        return null;
    }

    public Object visitCharacterExpression(CharacterExpression ast, Object o) {
        return null;
    }

    public Object visitEmptyExpression(EmptyExpression ast, Object o) {
        return null;
    }

    public Object visitFixedStringExpression(FixedStringExpression ast, Object o) {
        return null;
    }

    public Object visitIfExpression(IfExpression ast, Object o) {
        if (ast.invariant) {
            String uuid = UUID.randomUUID().toString();
            ((HashMap<String, Expression>) o).put(uuid, ast);
            ast.movedId = new VnameExpression(new SimpleVname(new Identifier(uuid, ast.position), ast.position), ast.position);
            ast.movedId.type = ast.type;
            return null;
        }
        ast.E1.visit(this, o);
        ast.E2.visit(this, o);
        ast.E3.visit(this, o);
        return null;
    }

    public Object visitIntegerExpression(IntegerExpression ast, Object o) {
        return null;
    }

    public Object visitLetExpression(LetExpression ast, Object o) {
        if (ast.invariant) {
            String uuid = UUID.randomUUID().toString();
            ((HashMap<String, Expression>) o).put(uuid, ast);
            ast.movedId = new VnameExpression(new SimpleVname(new Identifier(uuid, ast.position), ast.position), ast.position);
            ast.movedId.type = ast.type;
            return null;
        }
        ast.D.visit(this, o);
        ast.E.visit(this, o);
        return null;
    }

    public Object visitNilExpression(NilExpression ast, Object o) {
        return null;
    }

    public Object visitRecordExpression(RecordExpression ast, Object o) {
        if (ast.invariant) {
            String uuid = UUID.randomUUID().toString();
            ((HashMap<String, Expression>) o).put(uuid, ast);
            ast.movedId = new VnameExpression(new SimpleVname(new Identifier(uuid, ast.position), ast.position), ast.position);
            ast.movedId.type = ast.type;
            return null;
        }
        ast.RA.visit(this, o);
        return null;
    }

    public Object visitUnaryExpression(UnaryExpression ast, Object o) {
        if (ast.invariant) {
            String uuid = UUID.randomUUID().toString();
            ((HashMap<String, Expression>) o).put(uuid, ast);
            ast.movedId = new VnameExpression(new SimpleVname(new Identifier(uuid, ast.position), ast.position), ast.position);
            ast.movedId.type = ast.type;
            return null;
        }
        ast.O.visit(this, o);
        ast.E.visit(this, o);
        return null;
    }

    public Object visitVnameExpression(VnameExpression ast, Object o) {
        return null;
    }

    public Object visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Object o) {
        return null;
    }

    public Object visitConstDeclaration(ConstDeclaration ast, Object o) {
        ast.E.visit(this, o);
        return null;
    }

    public Object visitFuncDeclaration(FuncDeclaration ast, Object o) {
        return null;
    }

    public Object visitOpFuncDeclaration(OpFuncDeclaration ast, Object o) {
        return null;
    }

    public Object visitProcDeclaration(ProcDeclaration ast, Object o) {
        return null;
    }

    public Object visitSequentialDeclaration(SequentialDeclaration ast, Object o) {
        return null;
    }

    public Object visitTypeDeclaration(TypeDeclaration ast, Object o) {
        return null;
    }

    public Object visitRecTypeDeclaration(RecTypeDeclaration ast, Object o) {
        return null;
    }

    public Object visitEnumTypeDeclaration(EnumTypeDeclaration ast, Object o) {
        return null;
    }

    public Object visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Object o) {
        return null;
    }

    public Object visitVarDeclaration(VarDeclaration ast, Object o) {
        return null;
    }

    public Object visitVarInitialization(VarInitialization ast, Object o) {
        ast.E.visit(this, o);
        return null;
    }

    public Object visitPackageDeclaration(PackageDeclaration ast, Object o) {
        return null;
    }

    public Object visitMultipleArrayAggregate(MultipleArrayAggregate ast, Object o) {
        ast.E.visit(this, o);
        ast.AA.visit(this, o);
        return null;
    }

    public Object visitSingleArrayAggregate(SingleArrayAggregate ast, Object o) {
        ast.E.visit(this, o);
        return null;
    }

    public Object visitMultipleRecordAggregate(MultipleRecordAggregate ast, Object o) {
        ast.E.visit(this, o);
        ast.RA.visit(this, o);
        return null;
    }

    public Object visitSingleRecordAggregate(SingleRecordAggregate ast, Object o) {
        ast.E.visit(this, o);
        return null;
    }

    public Object visitIntegerLiteralCaseAggregate(IntegerLiteralCaseAggregate ast, Object o) {
        ast.C.visit(this, o);
        ast.CA.visit(this, o);
        return null;
    }

    public Object visitElseCaseAggregate(ElseCaseAggregate ast, Object o) {
        ast.C.visit(this, o);
        return null;
    }

    public Object visitConstFormalParameter(ConstFormalParameter ast, Object o) {
        return null;
    }

    public Object visitFuncFormalParameter(FuncFormalParameter ast, Object o) {
        return null;
    }

    public Object visitProcFormalParameter(ProcFormalParameter ast, Object o) {
        return null;
    }

    public Object visitVarFormalParameter(VarFormalParameter ast, Object o) {
        return null;
    }

    public Object visitValResFormalParameter(ValResFormalParameter ast, Object o) {
        return null;
    }

    public Object visitResFormalParameter(ResFormalParameter ast, Object o) {
        return null;
    }

    public Object visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Object o) {
        return null;
    }

    public Object visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Object o) {
        return null;
    }

    public Object visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Object o) {
        return null;
    }

    public Object visitConstActualParameter(ConstActualParameter ast, Object o) {
        ast.E.visit(this, o);
        return null;
    }

    public Object visitFuncActualParameter(FuncActualParameter ast, Object o) {
        return null;
    }

    public Object visitProcActualParameter(ProcActualParameter ast, Object o) {
        return null;
    }

    public Object visitVarActualParameter(VarActualParameter ast, Object o) {
        return null;
    }

    public Object visitValResActualParameter(ValResActualParameter ast, Object o) {
        return null;
    }

    public Object visitResActualParameter(ResActualParameter ast, Object o) {
        return null;
    }

    public Object visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Object o) {
        return null;
    }

    public Object visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Object o) {
        ast.AP.visit(this, o);
        ast.APS.visit(this, o);
        return null;
    }

    public Object visitSingleActualParameterSequence(SingleActualParameterSequence ast, Object o) {
        ast.AP.visit(this, o);
        return null;
    }

    public Object visitAnyTypeDenoter(AnyTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitArrayTypeDenoter(ArrayTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitBoolTypeDenoter(BoolTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitCharTypeDenoter(CharTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitEnumTypeDenoter(EnumTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitErrorTypeDenoter(ErrorTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitNilTypeDenoter(NilTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitSimpleTypeDenoter(SimpleTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitFixedStringTypeDenoter(FixedStringTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitIntTypeDenoter(IntTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitRecordTypeDenoter(RecordTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitMultipleEnumTypeDenoter(MultipleEnumTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitSingleEnumTypeDenoter(SingleEnumTypeDenoter ast, Object o) {
        return null;
    }

    public Object visitCharacterLiteral(CharacterLiteral ast, Object o) {
        return null;
    }

    public Object visitFixedStringLiteral(FixedStringLiteral ast, Object o) {
        return null;
    }

    public Object visitIdentifier(Identifier ast, Object o) {
        return null;
    }

    public Object visitPackagedIdentifier(PackagedIdentifier ast, Object o) {
        return null;
    }

    public Object visitIntegerLiteral(IntegerLiteral ast, Object o) {
        return null;
    }

    public Object visitOperator(Operator ast, Object o) {
        return null;
    }

    public Object visitDotVname(DotVname ast, Object o) {
        ast.V.visit(this, o);
        return null;
    }

    public Object visitSimpleVname(SimpleVname ast, Object o) {
        return null;
    }

    public Object visitSubscriptVname(SubscriptVname ast, Object o) {
        ast.V.visit(this, o);
        ast.E.visit(this, o);
        return null;
    }

    public Object visitProgram(Program ast, Object o) {
        ast.C.visit(this, o);
        return null;
    }
}
