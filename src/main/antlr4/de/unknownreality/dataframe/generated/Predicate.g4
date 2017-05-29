/*
 * Copyright (c) 2016 Alexander Grün
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

grammar Predicate;
 
/*
 * Parser Rules
 */

compilationUnit   : predicate EOF ;

field_filter :
NEGATE? OPEN_BRACKET field_filter CLOSE_BRACKET|
VAR FIELD_OPERATION value |
regex_filter;


regex_filter :
VAR MATCH REGEX;

predicate:
NEGATE? OPEN_BRACKET predicate CLOSE_BRACKET |
OPEN_BRACKET predicate PREDICATE_OPERATION predicate CLOSE_BRACKET|
predicate PREDICATE_OPERATION predicate|
field_filter;


value: NUMBER | BOOLEAN_VALUE | TEXT_VALUE;

/*
 * Lexer Rules
 */
fragment DIGIT : [0-9];
fragment CHAR : [a-zA-Z];
fragment STRING : '\'' ~('\'')* '\'';

fragment VAR_NAME :CHAR+[a-zA-Z0-9]*;
fragment UNESCAPED_STRING :~('\''|' '|')' | '(')+;

fragment EQ : ('EQ' | 'eq' | '=' | '==');
fragment NE : ('NE' | 'ne' | '!=');
fragment LE : ('LE' | 'le' | '<=');
fragment LT : ('LT' | 'lt' | '<');
fragment GE : ('GE' | 'ge' | '>=');
fragment GT : ('GT' | 'gt' | '>');

REGEX : '/' (~('/') | '\\/')+ '/';
MATCH : ('~=' | '~' );


fragment AND : ('AND' | 'and' | '&' | '&&');
fragment OR : ('OR' | 'or' | '|' | '||');
fragment XOR : ('XOR' | 'xor') ;
fragment NOR : ('NOR' | 'nor') ;
OPEN_BRACKET: '(';
CLOSE_BRACKET: ')';

NEGATE: '^';

PREDICATE_OPERATION : AND | OR | XOR | NOR;

FIELD_OPERATION : EQ | NE |LE | LT | GT | GE;

NUMBER : '-'? DIGIT+([.,]DIGIT+)?;
BOOLEAN_VALUE: 'true' | 'false';
TEXT_VALUE : STRING ;
VAR : UNESCAPED_STRING ('.' (UNESCAPED_STRING | STRING))? | STRING;

WHITESPACE : ' ' -> skip ;
