zserio_compatibility_version("2.4.0");

package test.sample;

import test.other.Type;
import test.other.sub.*;

/** Documentation comment. */
const uint8 SMALL = 0x20;
const uint32 EXPRESSIONS = 1 + 2 * 3 - 4 / 2 % 3;
const bool LOGIC = (1 << 3) >= 8 && !false || ~0 == -1 && 5 != 6;
const uint32 TERNARY = 5 > 3 ? 1 : 0;
const uint32 SHIFT = 256 >> 4;
const uint32 BITS = 0xFF & 0x0F | 0x10 ^ 0x01;
const uint32 BINARY = 101b;
const uint32 OCTAL = 017;
const float64 DOUBLE = 1.5e3;
const float32 SINGLE = 2.5f;
const string TEXT = "text with \"escapes\" \n";
const bool FLAG = true;

/*! Markdown documentation comment !*/
rule_group TestRules
{
    rule "text";
    rule "other";
};

subtype uint8 Byte;
subtype Pair<uint8, string> ByteAndString;

enum uint8 Kind
{
    @deprecated OLD,
    A = 1,
    B,
    @removed C,
    D = SMALL + 1,
};

enum bit:4 Small
{
    ONE = 1,
    TWO
};

bitmask uint16 Flags
{
    READ,
    WRITE = 2,
    ALL = READ | WRITE
};

struct Pair<T, U>
{
    T first;
    U second;
};

instantiate Pair<uint8, string> Uint8StringPair;

struct Wrapper<T>
{
    Pair<Pair<uint8, uint8>, T> nested;
};

struct Simple
{
    uint8 a;
    int16 b;
    varuint32 c;
    string d;
    bool e;
    float32 f;
    extern g;
    bytes h;
    bit:5 fixed;
    int:8 signedFixed;
};

struct Features(uint8 version, Kind kind)
{
    uint8 length;
    bit<length> dynamic;
    int<length + 1> signedDynamic;
    uint8 bounded : bounded < 10;
    uint8 initialized = 5;
    optional uint8 maybe;
    uint8 conditional if version >= 2;
    extend uint8 extension;
    align(8):
    uint8 aligned;
    uint32 offsets[3];
    offsets[0]: uint8 first;
    uint32 offsetsEnd[];
    offsetsEnd[@index]: uint8 indexed[];
    Simple fixedArray[4];
    Simple autoArray[];
    implicit uint8 implicitArray[];
    packed Simple packedArray[];
    test.other.Type qualified;
    Pair<uint8, Kind> templated : templated.first > 0;
    Simple item;
    Wrapper<string> wrapper;

    function uint32 total()
    {
        return length + (version > 1 ? 1 : 0) + lengthof(autoArray) + numbits(length) + valueof(kind);
    }

    function bool hasRead()
    {
        return isset(flags, Flags.READ) && item.a == fixedArray[0].a && total() > 5;
    }

    Flags flags;
};

struct Arguments(Kind kind, uint8 count)
{
    Features(1, Kind.A) features;
    Features(explicit count, kind) explicitFeatures;
};

choice Selector(Kind kind) on kind
{
    case Kind.A:
        uint8 a;
    case Kind.B:
    case Kind.D:
        string other : other != "";
    default:
        ;

    function bool isA()
    {
        return kind == Kind.A;
    }
};

choice Plain(uint8 selector) on selector
{
    case 1:
        uint8 one;
    default:
        uint16 other;
};

union Variant
{
    uint8 byteValue;
    string stringValue;
    packed Simple structValue;

    function uint8 getByte()
    {
        return byteValue;
    }
};

sql_table Users
{
    int64 id sql "PRIMARY KEY NOT NULL";
    string name sql "NOT NULL";
    sql_virtual uint8 hidden;
    Kind kind;
    sql "PRIMARY KEY(id)";
    sql_without_rowid;
};

sql_table Searchable using fts4
{
    string text;
};

sql_table GenericTable<T>
{
    T value;
};

sql_database Db
{
    Users users;
    Searchable search;
};

service Calculator
{
    Simple compute(Simple);
};

pubsub Chat
{
    publish topic("chat/out") Simple outgoing;
    subscribe topic("chat/in") Simple incoming;
    topic("chat/both") Simple both;
};
