package demo;

import <fold text='...'>demo.a.A;
import demo.b.B;</fold>

<fold text='/**...*/'>/**
 * Documentation.
 */</fold>
struct S <fold text='{...}'>{
    uint8 a;

    function uint8 get() <fold text='{...}'>{
        return a;
    }</fold>
}</fold>;

struct OneLine { uint8 a; };
