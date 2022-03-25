import com.intellij.database.model.DasTable
import com.intellij.database.model.ObjectKind
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

typeMapping = [
        (~/(?i)^bit$|boolean|tinyint\(1\)/)               : "bool",
        (~/(?i)^tinyint$/)                                : "byte",
        (~/(?i)^uniqueidentifier|uuid$/)                  : "Guid",
        (~/(?i)^int|integer$/)                            : "int",
        (~/(?i)^bigint$/)                                 : "long",
        (~/(?i)^varbinary|image$/)                        : "byte[]",
        (~/(?i)^double|float|real$/)                      : "double",
        (~/(?i)^decimal|money|numeric|smallmoney$/)       : "decimal",
        (~/(?i)^datetimeoffset$/)                         : "DateTimeOffset",
        (~/(?i)^datetime|datetime2|timestamp|date|time$/) : "DateTime",
        (~/(?i)^char$/)                                   : "char",
]

notNullableTypes = [ "string", "byte[]" ]
tempString = '';

SELECTION.filter { it instanceof DasTable && it.getKind() == ObjectKind.TABLE }.each { generate(it) }
CLIPBOARD.set(tempString)

def generate(table) {
    def className = pascalCase(table.getName())
    def fields = calcFields(table)

    StringWriter out = new StringWriter();

    if (tempString != '') {
        out.println ""
    }

    generate(out, className, fields, table);
        
    tempString += out.toString();
}

def generate(out, className, fields, table) {
    out.println "export default interface $className {"

    fields.each() {

        if (it.comment != "")
        {
            out.println "";
            out.println "    //${it.comment}";
        }
        
        def line = "    ${it.name}: ${it.type}"

        if (it.comment != "")
        {
            line += " // ${it.comment}"
        }

        out.println "${line}"
    }
    out.println "}"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def isArray = spec.contains('[]')
        def typeStr = typeMapping.find { p, t -> p.matcher(spec.replace("[]", "")).find() }?.value ?: "string"

        if (isArray) 
        {
            typeStr = "${typeStr}[]"
        }

        def nullable = col.isNotNull() || typeStr in notNullableTypes ? "" : "?"
        def pk = DasUtil.getPrimaryKey(table).toString();
        

        fields += [[
                           primarykey : pk != null && pk != "" && pk.contains("(${col.getName()})") ? true : false,
                           colname : col.getName(),
                           spec : spec,
                           name : camelCase(col.getName())  + nullable,
                           type : typeStr,
                           comment : col.comment ? col.comment : ""]]
    }
}

def camelCase(str) {
    def dict = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str).collect()
    def result = '';

    dict.forEach{ value ->
        if (result == '')
            result += value;
        else result += value.capitalize()
    }
    
    return result;
}

def pascalCase(str) {
    com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
            .collect { Case.LOWER.apply(it).capitalize() }
            .join("")
}