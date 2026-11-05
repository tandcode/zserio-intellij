const uint8 <info textAttributesKey="ZSERIO_CONSTANT">LIMIT</info> = 5;

enum uint8 <info textAttributesKey="ZSERIO_ENUM_NAME">Color</info> { <info textAttributesKey="ZSERIO_ENUM_ITEM">RED</info>, <info textAttributesKey="ZSERIO_ENUM_ITEM">GREEN</info> };

struct <info textAttributesKey="ZSERIO_TYPE_NAME">S</info>(uint8 <info textAttributesKey="ZSERIO_PARAMETER">version</info>)
{
    <info textAttributesKey="ZSERIO_ENUM_NAME">Color</info> <info textAttributesKey="ZSERIO_FIELD">color</info>;
    uint8 <info textAttributesKey="ZSERIO_FIELD">x</info> if <info textAttributesKey="ZSERIO_PARAMETER">version</info> > <info textAttributesKey="ZSERIO_CONSTANT">LIMIT</info> && <info textAttributesKey="ZSERIO_FIELD">color</info> == <info textAttributesKey="ZSERIO_ENUM_NAME">Color</info>.<info textAttributesKey="ZSERIO_ENUM_ITEM">RED</info>;

    function uint8 <info textAttributesKey="ZSERIO_FUNCTION">get</info>()
    {
        return <info textAttributesKey="ZSERIO_FIELD">x</info>;
    }
};

struct <info textAttributesKey="ZSERIO_TYPE_NAME">Box</info><<info textAttributesKey="ZSERIO_TEMPLATE_PARAMETER">T</info>>
{
    <info textAttributesKey="ZSERIO_TEMPLATE_PARAMETER">T</info> <info textAttributesKey="ZSERIO_FIELD">value</info>;
    <info textAttributesKey="ZSERIO_TYPE_NAME">Box</info><<info textAttributesKey="ZSERIO_TEMPLATE_PARAMETER">T</info>> <info textAttributesKey="ZSERIO_FIELD">self</info>;
};

choice <info textAttributesKey="ZSERIO_CHOICE_NAME">Pick</info>(uint8 <info textAttributesKey="ZSERIO_PARAMETER">selector</info>) on <info textAttributesKey="ZSERIO_PARAMETER">selector</info>
{
    case 1:
        uint8 <info textAttributesKey="ZSERIO_FIELD">one</info>;
    default:
        ;
};
