package plc.project;

import java.io.PrintWriter;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("public class Main {");
        newline(0);
        if (!ast.getFields().isEmpty()) {
            ++indent;
            for (int i = 0; i < ast.getFields().size(); i++) {
                newline(indent);
                print(ast.getFields().get(i));
            }
            newline(0);
            --indent;
        }
        newline(++indent);
        print("public static void main(String[] args) {");
        newline(++indent);
        print("System.exit(new Main().main());");
        newline(--indent);
        print("}");
        newline(0);
        for (int i = 0; i < ast.getMethods().size(); i++) {
            newline(indent);
            print(ast.getMethods().get(i));
            newline(0);
        }
        newline(--indent);
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        //throw new UnsupportedOperationException(); //TODO
        print(ast.getVariable().getType().getJvmName(), " ", ast.getVariable().getJvmName());
        if (ast.getValue().isPresent()){
            print(" = ", ast.getValue().get());
        }
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        //throw new UnsupportedOperationException(); //TODO
        print(ast.getFunction().getReturnType().getJvmName()," ",ast.getName(),"(");
        for (int i = 0; i < ast.getParameters().size(); i++) {
            print(Environment.getType(ast.getParameterTypeNames().get(i)).getJvmName()," ",ast.getParameters().get(i));
            if (!(i == ast.getParameters().size() - 1)) {
                print(", ");
            }
        }
        print(") {");

        if (!ast.getStatements().isEmpty())
        {
            newline(++indent);
            for (int i = 0; i < ast.getStatements().size(); i++) {
                if (i != 0){
                    newline(indent);
                }
                print(ast.getStatements().get(i));
            }
            newline(--indent);
        }
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        //throw new UnsupportedOperationException(); //TODO
        print(ast.getExpression(),";");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        //throw new UnsupportedOperationException(); //TODO
        print(ast.getVariable().getType().getJvmName()," ", ast.getVariable().getJvmName());
        if (ast.getValue().isPresent()){
            print(" = ", ast.getValue().get());
        }
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) {
        //throw new UnsupportedOperationException(); //TODO
        print(ast.getReceiver()," = ",ast.getValue(),";");
        return null;

    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("if (", ast.getCondition(), ") {");
        if (!ast.getThenStatements().isEmpty())
        {
            newline(++indent);
            for (int i = 0; i < ast.getThenStatements().size(); i++) {
                if (i != 0){
                    newline(indent);
                }
                print(ast.getThenStatements().get(i));
            }
            newline(--indent);
        }
        print("}");

        if (!ast.getElseStatements().isEmpty())
        {
            print(" else {");
            newline(++indent);
            for (int i = 0; i < ast.getElseStatements().size(); i++){
                if (i != 0){
                    newline(indent);
                }
                print(ast.getElseStatements().get(i));
            }
            newline(--indent);
            print("}");
        }
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("for (int ", ast.getName(), " : ", ast.getValue(), ") {");
        if (!ast.getStatements().isEmpty()) {
            newline(++indent);
            for (int i = 0; i < ast.getStatements().size(); i++) {
                if (i != 0) {
                    newline(indent);
                }
                print(ast.getStatements().get(i));
            }
            newline(--indent);
        }
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("while (",ast.getCondition(),") {");
        if (!ast.getStatements().isEmpty()){
            newline(++indent);
            for (int i = 0; i < ast.getStatements().size(); i++){
                if (i != 0){
                    newline(indent);
                }
                print(ast.getStatements().get(i));
            }
            newline(--indent);
        }
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("return ",ast.getValue(),";");
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) {
        //throw new UnsupportedOperationException(); //TODO
        if (ast.getLiteral().equals(Environment.NIL))
        {
            print("null");
            return null;
        }
        if (ast.getLiteral() instanceof String)
        {
            print("\"",ast.getLiteral(),"\"");
            return null;
        }
        if (ast.getLiteral() instanceof Character)
        {
            print("\'",ast.getLiteral(),"\'");
            return null;
        }
        print(ast.getLiteral());
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        //throw new UnsupportedOperationException(); //TODO
        print("(",ast.getExpression(),")");
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        //throw new UnsupportedOperationException(); //TODO
        print(ast.getLeft()," ");
        if (ast.getOperator().equals("AND")) {
            print("&&");
        }
        else if (ast.getOperator().equals("OR")) {
            print("||");
        }
        else {
            print(ast.getOperator());
        }
        print(" ", ast.getRight());

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        //throw new UnsupportedOperationException(); //TODO
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        //throw new UnsupportedOperationException(); //TODO
        return null;
    }

}
