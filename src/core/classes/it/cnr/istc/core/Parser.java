/*
 * Copyright (C) 2017 Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.istc.core;

import it.cnr.istc.common.Pair;
import it.cnr.istc.core.Lexer.RealToken;
import it.cnr.istc.core.Lexer.StringToken;
import static it.cnr.istc.core.Lexer.Symbol.*;
import it.cnr.istc.core.Lexer.Token;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class Parser {

    private Lexer lexer;
    private Token tk; // the current lookahead token..
    private final List<Token> tks = new ArrayList<>(); // all the tokens parsed so far..
    private int pos = 0; // the current position within 'tks'..

    Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private Token next() throws ParsingException {
        while (pos >= tks.size()) {
            Token c_tk = lexer.next();
            tks.add(c_tk);
        }
        return tks.get(pos++);
    }

    private boolean match(final Lexer.Symbol sym) throws ParsingException {
        if (tk.sym == sym) {
            tk = next();
            return true;
        } else {
            return false;
        }
    }

    private void backtrack(final int p) {
        pos = p;
        tk = tks.get(pos - 1);
    }

    CompilationUnit compilation_unit() throws ParsingException {
        tk = next();

        final List<MethodDeclaration> ms = new ArrayList<>();
        final List<PredicateDeclaration> ps = new ArrayList<>();
        final List<TypeDeclaration> ts = new ArrayList<>();
        final List<Statement> ss = new ArrayList<>();

        while (tk.sym != EOF) {
            switch (tk.sym) {
                case TYPEDEF:
                    ts.add(typedef_declaration());
                    break;
                case ENUM:
                    ts.add(enum_declaration());
                    break;
                case CLASS:
                    ts.add(class_declaration());
                    break;
                case PREDICATE:
                    ps.add(predicate_declaration());
                    break;
                case VOID:
                    ms.add(method_declaration());
                    break;
                case BOOL:
                case INT:
                case REAL:
                case STRING:
                case LBRACE:
                case BANG:
                case FACT:
                case GOAL:
                case IntLiteral:
                case RealLiteral:
                case StringLiteral:
                case TRUE:
                case FALSE:
                    ss.add(statement());
                    break;
                case ID: {
                    int c_pos = pos;
                    tk = next();
                    while (match(DOT)) {
                        if (!match(ID)) {
                            throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                        }
                    }
                    if (match(ID) && match(LPAREN)) {
                        backtrack(c_pos);
                        ms.add(method_declaration());
                    } else {
                        backtrack(c_pos);
                        ss.add(statement());
                    }
                    break;
                }
                default:
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected either 'typedef' or 'enum' or 'class' or 'predicate' or 'void' or identifier..");
            }
        }
        return new CompilationUnit(ms, ps, ts, ss);
    }

    private TypedefDeclaration typedef_declaration() throws ParsingException {
        String n;
        String pt;
        Expression xpr;

        if (!match(TYPEDEF)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected 'typedef'..");
        }

        switch (tk.sym) {
            case BOOL:
                pt = Type.BOOL;
                break;
            case INT:
                pt = Type.INT;
                break;
            case REAL:
                pt = Type.REAL;
                break;
            case STRING:
                pt = Type.STRING;
                break;
            default:
                throw new ParsingException(tk.start_line, tk.start_pos, "expected primitive type..");
        }
        tk = next();

        xpr = expression(0);

        if (!match(ID)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
        }
        n = ((Lexer.IdToken) tks.get(pos - 2)).id;

        if (!match(SEMICOLON)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected ';'..");
        }

        return new TypedefDeclaration(n, pt, xpr);
    }

    private EnumDeclaration enum_declaration() throws ParsingException {
        String n;
        List<String> es = new ArrayList<>();
        List<List<String>> trs = new ArrayList<>();

        if (!match(ENUM)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected 'enum'..");
        }

        if (!match(ID)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
        }
        n = ((Lexer.IdToken) tks.get(pos - 2)).id;

        do {
            switch (tk.sym) {
                case LBRACE:
                    tk = next();
                    if (!match(StringLiteral)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected string literal..");
                    }
                    es.add(((Lexer.StringToken) tks.get(pos - 2)).val);

                    while (match(COMMA)) {
                        if (!match(StringLiteral)) {
                            throw new ParsingException(tk.start_line, tk.start_pos, "expected string literal..");
                        }
                        es.add(((Lexer.StringToken) tks.get(pos - 2)).val);
                    }

                    if (!match(RBRACE)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected '}'..");
                    }
                    break;
                case ID: {
                    List<String> ids = new ArrayList<>();
                    ids.add(((Lexer.IdToken) tk).id);
                    tk = next();
                    while (match(DOT)) {
                        if (!match(ID)) {
                            throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                        }
                        ids.add(((Lexer.IdToken) tks.get(pos - 2)).id);
                    }
                    trs.add(ids);
                    break;
                }
                default:
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected either '{' or identifier..");
            }
        } while (match(BAR));

        if (!match(SEMICOLON)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected ';'..");
        }

        return new EnumDeclaration(n, es, trs);
    }

    private ClassDeclaration class_declaration() throws ParsingException {
        String n; // the name of the class..
        List<List<String>> bcs = new ArrayList<>(); // the base classes..
        List<FieldDeclaration> fs = new ArrayList<>(); // the fields of the class..
        List<ConstructorDeclaration> cs = new ArrayList<>(); // the constructors of the class..
        List<MethodDeclaration> ms = new ArrayList<>(); // the methods of the class..
        List<PredicateDeclaration> ps = new ArrayList<>(); // the predicates of the class..
        List<TypeDeclaration> ts = new ArrayList<>(); // the types of the class..

        if (!match(CLASS)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected 'class'..");
        }

        if (!match(ID)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
        }
        n = ((Lexer.IdToken) tks.get(pos - 2)).id;

        if (match(COLON)) {
            do {
                List<String> ids = new ArrayList<>();
                do {
                    if (!match(ID)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                    }
                    ids.add(((Lexer.IdToken) tks.get(pos - 2)).id);
                } while (match(DOT));
                bcs.add(ids);
            } while (match(COMMA));
        }

        if (!match(LBRACE)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected '{'..");
        }

        while (!match(RBRACE)) {
            switch (tk.sym) {
                case TYPEDEF:
                    ts.add(typedef_declaration());
                    break;
                case ENUM:
                    ts.add(enum_declaration());
                    break;
                case CLASS:
                    ts.add(class_declaration());
                    break;
                case PREDICATE:
                    ps.add(predicate_declaration());
                    break;
                case VOID:
                    ms.add(method_declaration());
                    break;
                case BOOL:
                case INT:
                case REAL:
                case STRING: // either a primitive type method or a field declaration..
                {
                    int c_pos = pos;
                    tk = next();
                    if (!match(ID)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                    }
                    switch (tk.sym) {
                        case LPAREN:
                            backtrack(c_pos);
                            ms.add(method_declaration());
                            break;
                        case EQ:
                        case SEMICOLON:
                            backtrack(c_pos);
                            fs.add(field_declaration());
                            break;
                        default:
                            throw new ParsingException(tk.start_line, tk.start_pos, "expected either '(' or '=' or ';'..");
                    }
                    break;
                }
                case ID: // either a constructor, a method or a field declaration..
                {
                    int c_pos = pos;
                    tk = next();
                    switch (tk.sym) {
                        case LPAREN:
                            backtrack(c_pos);
                            cs.add(constructor_declaration());
                            break;
                        case DOT:
                            while (match(DOT)) {
                                if (!match(ID)) {
                                    throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                                }
                            }
                            if (!match(ID)) {
                                throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                            }
                            switch (tk.sym) {
                                case LPAREN:
                                    backtrack(c_pos);
                                    ms.add(method_declaration());
                                    break;
                                case EQ:
                                case SEMICOLON:
                                    backtrack(c_pos);
                                    fs.add(field_declaration());
                                    break;
                                default:
                                    throw new ParsingException(tk.start_line, tk.start_pos, "expected either '(' or '=' or ';'..");
                            }
                            break;
                        case ID:
                            tk = next();
                            switch (tk.sym) {
                                case LPAREN:
                                    backtrack(c_pos);
                                    ms.add(method_declaration());
                                    break;
                                case EQ:
                                case SEMICOLON:
                                    backtrack(c_pos);
                                    fs.add(field_declaration());
                                    break;
                                default:
                                    throw new ParsingException(tk.start_line, tk.start_pos, "expected either '(' or '=' or ';'..");
                            }
                            break;
                        default:
                            throw new ParsingException(tk.start_line, tk.start_pos, "expected either '(' or '.' or an identifier..");
                    }
                    break;
                }
                default:
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected either 'typedef' or 'enum' or 'class' or 'predicate' or 'void' or identifier..");
            }
        }

        return new ClassDeclaration(n, bcs, fs, cs, ms, ps, ts);
    }

    private PredicateDeclaration predicate_declaration() throws ParsingException {
        String n;
        List<Pair<List<String>, String>> pars = new ArrayList<>();
        List<List<String>> pl = new ArrayList<>();
        List<Statement> stmnts = new ArrayList<>();

        if (!match(PREDICATE)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected 'predicate'..");
        }

        if (!match(ID)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
        }
        n = ((Lexer.IdToken) tks.get(pos - 2)).id;

        if (!match(LPAREN)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected '('..");
        }

        if (!match(RPAREN)) {
            do {
                List<String> p_ids = new ArrayList<>();
                switch (tk.sym) {
                    case BOOL:
                        p_ids.add(Type.BOOL);
                        tk = next();
                        break;
                    case INT:
                        p_ids.add(Type.INT);
                        tk = next();
                        break;
                    case REAL:
                        p_ids.add(Type.REAL);
                        tk = next();
                        break;
                    case STRING:
                        p_ids.add(Type.STRING);
                        tk = next();
                        break;
                    case ID:
                        p_ids.add(((Lexer.IdToken) tk).id);
                        tk = next();
                        while (match(DOT)) {
                            if (!match(ID)) {
                                throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                            }
                            p_ids.add(((Lexer.IdToken) tks.get(pos - 2)).id);
                        }
                        break;
                }
                if (!match(ID)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                }
                String pn = ((Lexer.IdToken) tks.get(pos - 2)).id;
                pars.add(new Pair<>(p_ids, pn));
            } while (match(COMMA));

            if (!match(RPAREN)) {
                throw new ParsingException(tk.start_line, tk.start_pos, "expected ')'..");
            }
        }

        if (match(COLON)) {
            do {
                List<String> p_ids = new ArrayList<>();
                do {
                    if (!match(ID)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                    }
                    p_ids.add(((Lexer.IdToken) tks.get(pos - 2)).id);
                } while (match(DOT));
                pl.add(p_ids);
            } while (match(COMMA));
        }

        if (!match(LBRACE)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected '{'..");
        }

        while (!match(RBRACE)) {
            stmnts.add(statement());
        }

        return new PredicateDeclaration(n, pars, pl, stmnts);
    }

    private ConstructorDeclaration constructor_declaration() throws ParsingException {
        List<Pair<List<String>, String>> pars = new ArrayList<>();
        List<Pair<String, List<Expression>>> il = new ArrayList<>();
        List<Statement> stmnts = new ArrayList<>();

        if (!match(ID)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
        }

        if (!match(LPAREN)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected '('..");
        }

        if (!match(RPAREN)) {
            do {
                List<String> p_ids = new ArrayList<>();
                switch (tk.sym) {
                    case ID:
                        p_ids.add(((Lexer.IdToken) tk).id);
                        tk = next();
                        while (match(DOT)) {
                            if (!match(ID)) {
                                throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                            }
                            p_ids.add(((Lexer.IdToken) tks.get(pos - 2)).id);
                        }
                        break;
                    case BOOL:
                        p_ids.add(Type.BOOL);
                        tk = next();
                        break;
                    case INT:
                        p_ids.add(Type.INT);
                        tk = next();
                        break;
                    case REAL:
                        p_ids.add(Type.REAL);
                        tk = next();
                        break;
                    case STRING:
                        p_ids.add(Type.STRING);
                        tk = next();
                        break;
                }
                if (!match(ID)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                }
                String pn = ((Lexer.IdToken) tks.get(pos - 2)).id;
                pars.add(new Pair<>(p_ids, pn));
            } while (match(COMMA));

            if (!match(RPAREN)) {
                throw new ParsingException(tk.start_line, tk.start_pos, "expected ')'..");
            }
        }

        if (match(COLON)) {
            do {
                String pn;
                List<Expression> xprs = new ArrayList<>();
                if (!match(ID)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                }
                pn = ((Lexer.IdToken) tks.get(pos - 2)).id;

                if (!match(LPAREN)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected '('..");
                }

                if (!match(RPAREN)) {
                    do {
                        xprs.add(expression(0));
                    } while (match(COMMA));

                    if (!match(RPAREN)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected ')'..");
                    }
                }
                il.add(new Pair<>(pn, xprs));
            } while (match(COMMA));
        }

        if (!match(LBRACE)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected '{'..");
        }

        while (!match(RBRACE)) {
            stmnts.add(statement());
        }

        return new ConstructorDeclaration(pars, il, stmnts);
    }

    private MethodDeclaration method_declaration() throws ParsingException {
        List<String> ids = new ArrayList<>();
        String n;
        List<Pair<List<String>, String>> pars = new ArrayList<>();
        List<Statement> stmnts = new ArrayList<>();

        if (!match(VOID)) {
            do {
                if (!match(ID)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                }
                ids.add(((Lexer.IdToken) tks.get(pos - 2)).id);
            } while (match(DOT));
        }

        if (!match(ID)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
        }
        n = ((Lexer.IdToken) tks.get(pos - 2)).id;

        if (!match(LPAREN)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected '('..");
        }

        if (!match(RPAREN)) {
            do {
                List<String> p_ids = new ArrayList<>();
                switch (tk.sym) {
                    case BOOL:
                        p_ids.add(Type.BOOL);
                        tk = next();
                        break;
                    case INT:
                        p_ids.add(Type.INT);
                        tk = next();
                        break;
                    case REAL:
                        p_ids.add(Type.REAL);
                        tk = next();
                        break;
                    case STRING:
                        p_ids.add(Type.STRING);
                        tk = next();
                        break;
                    case ID:
                        p_ids.add(((Lexer.IdToken) tk).id);
                        tk = next();
                        while (match(DOT)) {
                            if (!match(ID)) {
                                throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                            }
                            p_ids.add(((Lexer.IdToken) tks.get(pos - 2)).id);
                        }
                        break;
                    default:
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected either '" + Type.BOOL + "' or '" + Type.INT + "' or '" + Type.REAL + "' or '" + Type.STRING + "' or an identifier..");
                }
                if (!match(ID)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                }
                String pn = ((Lexer.IdToken) tks.get(pos - 2)).id;
                pars.add(new Pair<>(p_ids, pn));
            } while (match(COMMA));

            if (!match(RPAREN)) {
                throw new ParsingException(tk.start_line, tk.start_pos, "expected ')'..");
            }
        }

        if (!match(LBRACE)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected '{'..");
        }

        while (!match(RBRACE)) {
            stmnts.add(statement());
        }

        return new MethodDeclaration(ids, n, pars, stmnts);
    }

    private FieldDeclaration field_declaration() throws ParsingException {
        List<String> ids = new ArrayList<>();
        String n;
        List<VariableDeclaration> ds = new ArrayList<>();

        switch (tk.sym) {
            case BOOL:
                ids.add(Type.BOOL);
                tk = next();
                break;
            case INT:
                ids.add(Type.INT);
                tk = next();
                break;
            case REAL:
                ids.add(Type.REAL);
                tk = next();
                break;
            case STRING:
                ids.add(Type.STRING);
                tk = next();
                break;
            case ID:
                ids.add(((Lexer.IdToken) tk).id);
                tk = next();
                while (match(DOT)) {
                    if (!match(ID)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                    }
                    ids.add(((Lexer.IdToken) tks.get(pos - 2)).id);
                }
                break;
            default:
                throw new ParsingException(tk.start_line, tk.start_pos, "expected either '" + Type.BOOL + "' or '" + Type.INT + "' or '" + Type.REAL + "' or '" + Type.STRING + "' or an identifier..");
        }

        if (!match(ID)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
        }
        n = ((Lexer.IdToken) tks.get(pos - 2)).id;

        if (match(EQ)) {
            ds.add(new VariableDeclaration(n, expression(0)));
        } else {
            ds.add(new VariableDeclaration(n, null));
        }

        while (match(COMMA)) {
            if (!match(ID)) {
                throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
            }
            n = ((Lexer.IdToken) tks.get(pos - 2)).id;

            if (match(EQ)) {
                ds.add(new VariableDeclaration(n, expression(0)));
            } else {
                ds.add(new VariableDeclaration(n, null));
            }
        }

        if (!match(SEMICOLON)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected ';'..");
        }

        return new FieldDeclaration(ids, ds);
    }

    private Statement statement() throws ParsingException {
        switch (tk.sym) {
            case BOOL:
            case INT:
            case REAL:
            case STRING: // a local field having a primitive type..
            {
                List<String> ids = new ArrayList<>();
                switch (tk.sym) {
                    case BOOL:
                        ids.add("bool");
                        break;
                    case INT:
                        ids.add("int");
                        break;
                    case REAL:
                        ids.add("real");
                        break;
                    case STRING:
                        ids.add("string");
                        break;
                }
                tk = next();

                if (!match(ID)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                }
                String n = ((Lexer.IdToken) tks.get(pos - 2)).id;

                Expression e = null;
                if (tk.sym == EQ) {
                    tk = next();
                    e = expression(0);
                }

                if (!match(SEMICOLON)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected ';'..");
                }

                return new LocalFieldStatement(ids, n, e);
            }
            case ID: // either a local field, an assignment or an expression..
            {
                int c_pos = pos;
                List<String> ids = new ArrayList<>();
                ids.add(((Lexer.IdToken) tk).id);
                tk = next();
                while (match(DOT)) {
                    if (!match(ID)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                    }
                    ids.add(((Lexer.IdToken) tks.get(pos - 2)).id);
                }

                switch (tk.sym) {
                    case ID: // a local field..
                    {
                        String n = ((Lexer.IdToken) tk).id;
                        Expression e = null;
                        tk = next();
                        if (tk.sym == EQ) {
                            tk = next();
                            e = expression(0);
                        }

                        if (!match(SEMICOLON)) {
                            throw new ParsingException(tk.start_line, tk.start_pos, "expected ';'..");
                        }

                        return new LocalFieldStatement(ids, n, e);
                    }
                    case EQ: // an assignment..
                    {
                        String id = ids.remove(ids.size() - 1);
                        tk = next();
                        Expression xpr = expression(0);
                        if (!match(SEMICOLON)) {
                            throw new ParsingException(tk.start_line, tk.start_pos, "expected ';'..");
                        }
                        return new AssignmentStatement(ids, id, xpr);
                    }
                    case PLUS: // an expression..
                    case MINUS:
                    case STAR:
                    case SLASH:
                    case LT:
                    case LTEQ:
                    case EQEQ:
                    case GTEQ:
                    case GT:
                    case BANGEQ:
                    case IMPLICATION:
                    case BAR:
                    case AMP:
                    case CARET: {
                        backtrack(c_pos);
                        Expression xpr = expression(0);
                        if (!match(SEMICOLON)) {
                            throw new ParsingException(tk.start_line, tk.start_pos, "expected ';'..");
                        }
                        return new ExpressionStatement(xpr);
                    }
                    default:
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected either '=' or an identifier..");
                }
            }
            case LBRACE: // either a block or a disjunction..
            {
                tk = next();
                List<Statement> stmnts = new ArrayList<>();
                do {
                    stmnts.add(statement());
                } while (!match(RBRACE));
                switch (tk.sym) {
                    case LBRACKET:
                    case OR: // a disjunctive statement..
                    {
                        List<Pair<List<Statement>, Expression>> disjs = new ArrayList<>();
                        Expression e = null;
                        if (match(LBRACKET)) {
                            e = expression(0);
                            if (!match(RBRACKET)) {
                                throw new ParsingException(tk.start_line, tk.start_pos, "expected ']'..");
                            }
                        }
                        disjs.add(new Pair<>(new ArrayList<>(stmnts), e));
                        while (match(OR)) {
                            stmnts.clear();
                            e = null;
                            if (!match(LBRACE)) {
                                throw new ParsingException(tk.start_line, tk.start_pos, "expected '{'..");
                            }
                            do {
                                stmnts.add(statement());
                            } while (!match(RBRACE));
                            if (match(LBRACKET)) {
                                e = expression(0);
                                if (!match(RBRACKET)) {
                                    throw new ParsingException(tk.start_line, tk.start_pos, "expected ']'..");
                                }
                            }
                            disjs.add(new Pair<>(stmnts, e));
                        }
                        return new DisjunctionStatement(disjs);
                    }
                    default: // a block statement..
                        return new BlockStatement(stmnts);
                }
            }
            case FACT:
            case GOAL: {
                boolean isf = tk.sym == FACT;
                tk = next();
                String fn;
                List<String> scp = new ArrayList<>();
                String pn;
                List<Pair<String, Expression>> assgns = new ArrayList<>();

                if (!match(ID)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                }
                fn = ((Lexer.IdToken) tks.get(pos - 2)).id;

                if (!match(EQ)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected '='..");
                }

                if (!match(NEW)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected 'new'..");
                }

                do {
                    if (!match(ID)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                    }
                    scp.add(((Lexer.IdToken) tks.get(pos - 2)).id);
                } while (match(DOT));

                pn = scp.remove(scp.size() - 1);

                if (!match(LPAREN)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected '('..");
                }

                if (!match(RPAREN)) {
                    do {
                        if (!match(ID)) {
                            throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                        }
                        String assgn_name = ((Lexer.IdToken) tks.get(pos - 2)).id;

                        if (!match(COLON)) {
                            throw new ParsingException(tk.start_line, tk.start_pos, "expected ':'..");
                        }

                        Expression xpr = expression(0);
                        assgns.add(new Pair<>(assgn_name, xpr));
                    } while (match(COMMA));

                    if (!match(RPAREN)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected ')'..");
                    }
                }

                if (!match(SEMICOLON)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected ';'..");
                }
                return new FormulaStatement(isf, fn, scp, pn, assgns);
            }
            case RETURN: {
                Expression xpr = expression(0);
                if (!match(SEMICOLON)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected ';'..");
                }
                return new ReturnStatement(xpr);
            }
            default: {
                Expression xpr = expression(0);
                if (!match(SEMICOLON)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected ';'..");
                }
                return new ExpressionStatement(xpr);
            }
        }
    }

    private Expression expression(final int pr) throws ParsingException {
        Expression e;
        switch (tk.sym) {
            case TRUE:
            case FALSE:
                tk = next();
                e = new BoolLiteralExpression(tks.get(pos - 2).sym == TRUE);
                break;
            case IntLiteral:
                tk = next();
                e = new IntLiteralExpression(((Lexer.IntToken) tks.get(pos - 2)).val);
                break;
            case RealLiteral:
                tk = next();
                e = new RealLiteralExpression(((RealToken) tks.get(pos - 2)).val);
                break;
            case StringLiteral:
                tk = next();
                e = new StringLiteralExpression(((StringToken) tks.get(pos - 2)).val);
                break;
            case LPAREN: // either a parenthesys expression or a cast..
            {
                tk = next();

                int c_pos = pos;
                do {
                    if (!match(ID)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                    }
                } while (match(DOT));

                if (match(RPAREN)) // a cast..
                {
                    backtrack(c_pos);
                    List<String> ids = new ArrayList<>();
                    do {
                        if (!match(ID)) {
                            throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                        }
                        ids.add(((Lexer.IdToken) tks.get(pos - 2)).id);
                    } while (match(DOT));

                    if (!match(RPAREN)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected ')'..");
                    }
                    Expression xpr = expression(0);
                    e = new CastExpression(ids, xpr);
                } else // a parenthesis..
                {
                    backtrack(c_pos);
                    Expression xpr = expression(0);
                    if (!match(RPAREN)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected ')'..");
                    }
                    e = xpr;
                }
                break;
            }
            case PLUS:
                tk = next();
                e = new PlusExpression(expression(4));
                break;
            case MINUS:
                tk = next();
                e = new MinusExpression(expression(4));
                break;
            case BANG:
                tk = next();
                e = new NegationExpression(expression(4));
                break;
            case LBRACKET: {
                tk = next();
                Expression min_e = expression(0);
                Expression max_e = expression(0);

                if (!match(RBRACKET)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected ']'..");
                }

                e = new RangeExpression(min_e, max_e);
                break;
            }
            case NEW: {
                tk = next();
                Collection<String> ids = new ArrayList<>();
                do {
                    if (!match(ID)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                    }
                    ids.add(((Lexer.IdToken) tks.get(pos - 2)).id);
                } while (match(DOT));

                Collection<Expression> xprs = new ArrayList<>();
                if (!match(LPAREN)) {
                    throw new ParsingException(tk.start_line, tk.start_pos, "expected '('..");
                }

                if (!match(RPAREN)) {
                    do {
                        xprs.add(expression(0));
                    } while (match(COMMA));

                    if (!match(RPAREN)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected ')'..");
                    }
                }

                e = new ConstructorExpression(ids, xprs);
                break;
            }
            case ID: {
                List<String> is = new ArrayList<>();
                is.add(((Lexer.IdToken) tk).id);
                tk = next();
                while (match(DOT)) {
                    if (!match(ID)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
                    }
                    is.add(((Lexer.IdToken) tks.get(pos - 2)).id);
                }
                if (match(LPAREN)) {
                    tk = next();
                    String fn = is.remove(is.size() - 1);
                    List<Expression> xprs = new ArrayList<>();
                    if (!match(LPAREN)) {
                        throw new ParsingException(tk.start_line, tk.start_pos, "expected '('..");
                    }

                    if (!match(RPAREN)) {
                        do {
                            xprs.add(expression(0));
                        } while (match(COMMA));

                        if (!match(RPAREN)) {
                            throw new ParsingException(tk.start_line, tk.start_pos, "expected ')'..");
                        }
                    }

                    e = new FunctionExpression(is, fn, xprs);
                } else {
                    e = new IdExpression(is);
                }
                break;
            }
            default:
                throw new ParsingException(tk.start_line, tk.start_pos, "expected either '(' or '+' or '-' or '!' or '[' or 'new' or a literal or an identifier..");
        }

        while (((tk.sym == EQEQ || tk.sym == BANGEQ) && 0 >= pr)
                || ((tk.sym == LT || tk.sym == LTEQ || tk.sym == GTEQ || tk.sym == GT || tk.sym == IMPLICATION || tk.sym == BAR || tk.sym == AMP || tk.sym == CARET) && 1 >= pr)
                || ((tk.sym == PLUS || tk.sym == MINUS) && 2 >= pr)
                || ((tk.sym == STAR || tk.sym == SLASH) && 3 >= pr)) {
            switch (tk.sym) {
                case EQEQ:
                    assert (0 >= pr);
                    tk = next();
                    e = new EqExpression(e, expression(1));
                    break;
                case BANGEQ:
                    assert (0 >= pr);
                    tk = next();
                    e = new NeqExpression(e, expression(1));
                    break;
                case LT: {
                    assert (1 >= pr);
                    tk = next();
                    e = new LtExpression(e, expression(2));
                    break;
                }
                case LTEQ: {
                    assert (1 >= pr);
                    tk = next();
                    e = new LeqExpression(e, expression(2));
                    break;
                }
                case GTEQ: {
                    assert (1 >= pr);
                    tk = next();
                    e = new GeqExpression(e, expression(2));
                    break;
                }
                case GT: {
                    assert (1 >= pr);
                    tk = next();
                    e = new GtExpression(e, expression(2));
                    break;
                }
                case IMPLICATION: {
                    assert (1 >= pr);
                    tk = next();
                    e = new ImplicationExpression(e, expression(2));
                    break;
                }
                case BAR: {
                    assert (1 >= pr);
                    List<Expression> xprs = new ArrayList<>();
                    xprs.add(e);

                    while (match(BAR)) {
                        xprs.add(expression(2));
                    }

                    e = new DisjunctionExpression(xprs);
                    break;
                }
                case AMP: {
                    assert (1 >= pr);
                    List<Expression> xprs = new ArrayList<>();
                    xprs.add(e);

                    while (match(BAR)) {
                        xprs.add(expression(2));
                    }

                    e = new ConjunctionExpression(xprs);
                    break;
                }
                case CARET: {
                    assert (1 >= pr);
                    List<Expression> xprs = new ArrayList<>();
                    xprs.add(e);

                    while (match(BAR)) {
                        xprs.add(expression(2));
                    }

                    e = new ExctOneExpression(xprs);
                    break;
                }
                case PLUS: {
                    assert (2 >= pr);
                    List<Expression> xprs = new ArrayList<>();
                    xprs.add(e);

                    while (match(PLUS)) {
                        xprs.add(expression(3));
                    }

                    e = new AdditionExpression(xprs);
                    break;
                }
                case MINUS: {
                    assert (2 >= pr);
                    List<Expression> xprs = new ArrayList<>();
                    xprs.add(e);

                    while (match(MINUS)) {
                        xprs.add(expression(3));
                    }

                    e = new SubtractionExpression(xprs);
                    break;
                }
                case STAR: {
                    assert (3 >= pr);
                    List<Expression> xprs = new ArrayList<>();
                    xprs.add(e);

                    while (match(STAR)) {
                        xprs.add(expression(4));
                    }

                    e = new MultiplicationExpression(xprs);
                    break;
                }
                case SLASH: {
                    assert (3 >= pr);
                    List<Expression> xprs = new ArrayList<>();
                    xprs.add(e);

                    while (match(SLASH)) {
                        xprs.add(expression(4));
                    }

                    e = new DivisionExpression(xprs);
                    break;
                }
            }
        }

        return e;
    }
}
