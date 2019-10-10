import com.intellij.database.model.DasTable
import com.intellij.database.model.ObjectKind
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

typeMapping = [
        (~/(?i)^bit$|tinyint\(1\)/)                       : "boolean",
        (~/(?i)^tinyint$/)                                : "number",
        (~/(?i)^uniqueidentifier|uuid$/)                  : "string",
        (~/(?i)^int|integer$/)                            : "number",
        (~/(?i)^bigint$/)                                 : "number",
        (~/(?i)^varbinary|image$/)                        : "any[]",
        (~/(?i)^double|float|real$/)                      : "number",
        (~/(?i)^decimal|money|numeric|smallmoney$/)       : "number",
        (~/(?i)^datetimeoffset$/)                         : "Date",
        (~/(?i)^datetime|datetime2|timestamp|date|time$/) : "Date",
        (~/(?i)^char$/)                                   : "string",
]

notNullableTypes = [ "string", "byte[]" ]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable && it.getKind() == ObjectKind.TABLE }.each { generate(it, dir) }
}

def generate(table, dir) {
    def className = tsName(table.getName())
    def fields = calcFields(table)
    new File(dir, "I" + className + ".ts").withPrintWriter { out -> generate(out, className, fields, table) }
}

def generate(out, className, fields, table) {
    out.println "interface I$className"
    out.println "{"

    fields.each() {

        if (it.comment != "")
        {
            out.println "";
            out.println "    //${it.comment}";
        }
        
        def line = "    ${it.name} : ${it.type}"

        if (Case.LOWER.apply(it.colname) != Case.LOWER.apply(it.varName))
            line += " /* DB column: $it.colname */"

        out.println "${line}"
    }
    out.println "}"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }?.value ?: "string"
        def nullable = col.isNotNull() || typeStr in notNullableTypes ? "" : "?"
        def pk = DasUtil.getPrimaryKey(table).toString();

        fields += [[
                           primarykey : pk != null && pk != "" && pk.contains("(${col.getName()})") ? true : false,
                           colname : col.getName(),
                           spec : spec,
                           varName : tsName(col.getName()),
                           name : tsName(col.getName()) + nullable,
                           type : typeStr,
                           comment : col.comment ? col.comment : ""]]
    }
}

def tsName(str) {
    com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
            .collect { Case.LOWER.apply(it).capitalize() }
            .join("")
}
