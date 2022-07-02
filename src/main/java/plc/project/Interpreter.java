package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import plc.project.Environment.PlcObject;

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope scope = new Scope(null);

    public Interpreter(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", 1, args -> {
            System.out.println(args.get(0).getValue());
            return Environment.NIL;
        });
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Environment.PlcObject visit(Ast.Source ast) {

        //throw new UnsupportedOperationException(); //TODO
        {
            for (Ast.Field fields : ast.getFields())
            {
                visit(fields);
            }
            for (Ast.Method methods: ast.getMethods())
            {
                visit(methods);
            }
            return scope.lookupFunction("main", 0).invoke(Arrays.asList());
        }


    }

    @Override
    public Environment.PlcObject visit(Ast.Field ast) {
        //throw new UnsupportedOperationException(); //TODO
        if (ast.getValue().isPresent()) {
            scope.defineVariable(ast.getName(), visit(ast.getValue().get()));
        }
        else {
            scope.defineVariable(ast.getName(), Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Method ast) {
        //throw new UnsupportedOperationException(); //TODO
        {
            scope.defineFunction(ast.getName(), ast.getParameters().size(), args -> {
                try {
                    scope = new Scope(scope);
                    ast.getParameters().forEach(name -> {
                        args.forEach(value -> {
                            scope.defineVariable(name, value);
                        });
                    });
                    for (Ast.Stmt stmt: ast.getStatements()) {
                        visit(stmt);
                    }
                    return Environment.NIL;
                }
                catch (Return temp) {
                    return temp.value;
                }
                finally {
                    scope = scope.getParent();
                }
            });
            return Environment.NIL;
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Expression ast) {
        //throw new UnsupportedOperationException(); //TODO
        visit(ast.getExpression());
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Declaration ast) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        if (ast.getValue().isPresent()) {
            scope.defineVariable (ast.getName(), visit (ast.getValue().get()));
        }
        else {
            scope.defineVariable (ast.getName(), Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Assignment ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.If ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.For ast) {
        //throw new UnsupportedOperationException(); //TODO
        Iterable item = requireType(Iterable.class, visit(ast.getValue()));
        item.forEach( newItem -> {
            try {
                scope = new Scope(scope);
                scope.defineVariable(ast.getName(), PlcObject.class.cast(newItem));
                ast.getStatements().forEach( one -> {
                    visit(one);
                });
            }
            finally {
                scope = scope.getParent();
            }
        });
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.While ast) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        while (requireType(Boolean.class, visit(ast.getCondition()))) {
            try {
                scope = new Scope(scope);
                for (Ast.Stmt stmt : ast.getStatements()) {
                    visit(stmt);
                }
            }
            finally {
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }



    @Override
    public Environment.PlcObject visit(Ast.Stmt.Return ast) {
        //throw new UnsupportedOperationException(); //TODO
        Environment.PlcObject value = visit(ast.getValue());
        throw new Return(value);
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Literal ast) {
        //throw new UnsupportedOperationException(); //TODO
        if(ast.getLiteral() == null){
            return Environment.NIL;
        }
        else {
            return Environment.create(ast.getLiteral());
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Group ast) {
        //throw new UnsupportedOperationException(); //TODO
        return visit(ast.getExpression());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Binary ast) {
        //throw new UnsupportedOperationException(); //TODO
        switch (ast.getOperator()) {
            case "AND":
                if (requireType(Boolean.class, visit(ast.getLeft()))) {
                    if (requireType(Boolean.class, visit(ast.getRight()))) {
                        return Environment.create(true);
                    }
                }
                return Environment.create(false);

            case "OR":
                if(requireType(Boolean.class, visit(ast.getLeft()))) {
                    return Environment.create(true);
                }
                else {
                    if(requireType(Boolean.class, visit(ast.getRight()))) {
                        return Environment.create(true);
                    }
                    return Environment.create(false);
                }

            case "<":
                if(visit(ast.getLeft()).getValue().getClass() != visit(ast.getRight()).getValue().getClass()) {
                    throw new RuntimeException();
                }
                if(requireType(Comparable.class, visit(ast.getLeft())).compareTo(requireType(Comparable.class, visit(ast.getRight()))) < 0) {
                    return Environment.create(true);
                }
                return Environment.create(false);

            case ">=":
                if(visit(ast.getLeft()).getValue().getClass() != visit(ast.getRight()).getValue().getClass()) {
                    throw new RuntimeException();
                }
                if(requireType(Comparable.class, visit(ast.getLeft())).compareTo(requireType(Comparable.class, visit(ast.getRight()))) >= 0) {
                    return Environment.create(true);
                }
                return Environment.create(false);

            case "==":
                if(visit(ast.getLeft()).getValue().equals(visit(ast.getRight()).getValue())) {
                    return Environment.create(true);
                }
                return Environment.create(false);

            case "+":
                if(visit(ast.getLeft()).getValue() instanceof String || visit(ast.getRight()).getValue() instanceof String) {
                    return Environment.create((String)visit(ast.getLeft()).getValue() + (String)visit(ast.getRight()).getValue());
                }
                else if(visit(ast.getLeft()).getValue() instanceof BigInteger && visit(ast.getRight()).getValue() instanceof BigInteger) {
                    BigInteger result = requireType(BigInteger.class, visit(ast.getLeft())).add(requireType(BigInteger.class, visit(ast.getRight())));
                    return Environment.create(result);
                }
                else if(visit(ast.getLeft()).getValue() instanceof BigDecimal && visit(ast.getRight()).getValue() instanceof BigDecimal) {
                    BigDecimal result = requireType(BigDecimal.class, visit(ast.getLeft())).add(requireType(BigDecimal.class, visit(ast.getRight())));
                    return Environment.create(result);
                }
                throw new RuntimeException();

            case "/":
                if(visit(ast.getLeft()).getValue() instanceof BigInteger && visit(ast.getRight()).getValue() instanceof BigInteger) {
                    if(requireType(BigInteger.class, visit(ast.getRight())).equals(0)) {
                        throw new RuntimeException();
                    }

                    BigInteger result = requireType(BigInteger.class, visit(ast.getLeft())).divide(requireType(BigInteger.class, visit(ast.getRight())));
                    return Environment.create(result);
                }
                else if(visit(ast.getLeft()).getValue() instanceof BigDecimal && visit(ast.getRight()).getValue() instanceof BigDecimal) {
                    if(requireType(BigDecimal.class, visit(ast.getRight())).equals(0)) {
                        throw new RuntimeException();
                    }

                    BigDecimal result = requireType(BigDecimal.class, visit(ast.getLeft())).divide(requireType(BigDecimal.class, visit(ast.getRight())), 1, RoundingMode.HALF_EVEN);
                    return Environment.create(result);
                }
                throw new RuntimeException();

            case "-":
                if(visit(ast.getLeft()).getValue() instanceof BigInteger && visit(ast.getRight()).getValue() instanceof BigInteger) {
                    BigInteger result = requireType(BigInteger.class, visit(ast.getLeft())).subtract(requireType(BigInteger.class, visit(ast.getRight())));
                    return Environment.create(result);
                }
                else if(visit(ast.getLeft()).getValue() instanceof BigDecimal && visit(ast.getRight()).getValue() instanceof BigDecimal) {
                    BigDecimal result = requireType(BigDecimal.class, visit(ast.getLeft())).subtract(requireType(BigDecimal.class, visit(ast.getRight())));
                    return Environment.create(result);
                }
                throw new RuntimeException();

            case "*":
                if(visit(ast.getLeft()).getValue() instanceof BigInteger && visit(ast.getRight()).getValue() instanceof BigInteger) {
                    BigInteger result = requireType(BigInteger.class, visit(ast.getLeft())).multiply(requireType(BigInteger.class, visit(ast.getRight())));
                    return Environment.create(result);
                }
                else if(visit(ast.getLeft()).getValue() instanceof BigDecimal && visit(ast.getRight()).getValue() instanceof BigDecimal) {
                    BigDecimal result = requireType(BigDecimal.class, visit(ast.getLeft())).multiply(requireType(BigDecimal.class, visit(ast.getRight())));
                    return Environment.create(result);
                }
                throw new RuntimeException();

            case "<=":
                if(visit(ast.getLeft()).getValue().getClass() != visit(ast.getRight()).getValue().getClass()) {
                    throw new RuntimeException();
                }
                if(requireType(Comparable.class, visit(ast.getLeft())).compareTo(requireType(Comparable.class, visit(ast.getRight()))) <= 0) {
                    return Environment.create(true);
                }
                return Environment.create(false);

            case ">":
                if(visit(ast.getLeft()).getValue().getClass() != visit(ast.getRight()).getValue().getClass()) {
                    throw new RuntimeException();
                }
                if(requireType(Comparable.class, visit(ast.getLeft())).compareTo(requireType(Comparable.class, visit(ast.getRight()))) > 0) {
                    return Environment.create(true);
                }
                return Environment.create(false);
        }
        throw new RuntimeException();
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Access ast) {
        //throw new UnsupportedOperationException(); //TODO
        if (ast.getReceiver().isPresent()) {
            return visit(ast.getReceiver().get()).getField(ast.getName()).getValue();
        }
        return scope.lookupVariable(ast.getName()).getValue();
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Function ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Helper function to ensure an object is of the appropriate type.
     */
    private static <T> T requireType(Class<T> type, Environment.PlcObject object) {
        if (type.isInstance(object.getValue())) {
            return type.cast(object.getValue());
        } else {
            throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
        }
    }

    /**
     * Exception class for returning values.
     */
    private static class Return extends RuntimeException {

        private final Environment.PlcObject value;

        private Return(Environment.PlcObject value) {
            this.value = value;
        }

    }

}
