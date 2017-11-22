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
package it.cnr.istc.parser;

import static it.cnr.istc.parser.Lexer.Symbol.*;
import it.cnr.istc.parser.Lexer.Token;
import it.cnr.istc.parser.declarations.ClassDeclaration;
import it.cnr.istc.parser.declarations.CompilationUnit;
import it.cnr.istc.parser.declarations.EnumDeclaration;
import it.cnr.istc.parser.declarations.MethodDeclaration;
import it.cnr.istc.parser.declarations.PredicateDeclaration;
import it.cnr.istc.parser.declarations.TypeDeclaration;
import it.cnr.istc.parser.declarations.TypedefDeclaration;
import it.cnr.istc.parser.expressions.Expression;
import it.cnr.istc.parser.statements.Statement;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Parser {

    private Lexer lexer;
    private Token tk; // the current lookahead token..
    private final List<Token> tks = new ArrayList<>(); // all the tokens parsed so far..
    private int pos = 0; // the current position within 'tks'..

    public CompilationUnit parse(final Reader r) throws IOException, ParsingException {
        lexer = new Lexer(r);
        tk = lexer.next();

        final Collection<MethodDeclaration> ms = new ArrayList<>();
        final Collection<PredicateDeclaration> ps = new ArrayList<>();
        final Collection<TypeDeclaration> ts = new ArrayList<>();
        final Collection<Statement> ss = new ArrayList<>();

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

    private TypedefDeclaration typedef_declaration() throws ParsingException {
        String n;
        String pt;
        Expression xpr;

        if (!match(TYPEDEF)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected 'typedef'..");
        }

        switch (tk.sym) {
            case BOOL:
                pt = "bool";
                break;
            case INT:
                pt = "int";
                break;
            case REAL:
                pt = "real";
                break;
            case STRING:
                pt = "string";
                break;
            default:
                throw new ParsingException(tk.start_line, tk.start_pos, "expected primitive type..");
        }
        tk = next();

        xpr = expression();

        if (!match(ID)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected identifier..");
        }
        n = ((Lexer.IdToken) tks.get(pos - 2)).id;

        if (!match(SEMICOLON)) {
            throw new ParsingException(tk.start_line, tk.start_pos, "expected ';'..");
        }

        return new TypedefDeclaration(n, pt, xpr);
    }

    private EnumDeclaration enum_declaration() {
        throw new UnsupportedOperationException("not supported yet..");
    }

    private ClassDeclaration class_declaration() {
        throw new UnsupportedOperationException("not supported yet..");
    }

    private PredicateDeclaration predicate_declaration() {
        throw new UnsupportedOperationException("not supported yet..");
    }

    private MethodDeclaration method_declaration() {
        throw new UnsupportedOperationException("not supported yet..");
    }

    private Statement statement() {
        throw new UnsupportedOperationException("not supported yet..");
    }

    private Expression expression() {
        throw new UnsupportedOperationException("not supported yet..");
    }
}
