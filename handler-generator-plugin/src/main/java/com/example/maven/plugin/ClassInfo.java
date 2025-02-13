package com.example.mavenplugin;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
// Helper class to store class information
public class ClassInfo {
    String simpleName;
    String packageName;
    String fullName;
    List<ConstructorInfo> constructors = new ArrayList<ConstructorInfo>();

    public ClassInfo(String simpleName,String packageName) {
        this.simpleName = simpleName;
        this.packageName = packageName;
        this.fullName = packageName + "." + simpleName;
    }

    static Map<String, String> mappings;

    static {
        mappings = new HashMap<>();
        mappings.put("java.lang.String[]", "ARRAY");
        mappings.put("java.lang.String", "VARCHAR");
        mappings.put("String", "VARCHAR");
        mappings.put("boolean", "BOOLEAN");
        mappings.put("Boolean", "BOOLEAN");
        mappings.put("java.lang.Boolean", "BOOLEAN");
        mappings.put("Date", "DATE");
        mappings.put("java.sql.Date", "DATE");
        mappings.put("java.util.Date", "DATE");
        mappings.put("int", "NUMBER");
        mappings.put("Integer", "NUMBER");
        mappings.put("java.lang.Integer", "NUMBER");
        mappings.put("long", "NUMBER");
        mappings.put("Long", "NUMBER");
        mappings.put("java.lang.Long", "NUMBER");
        mappings.put("double", "FLOAT");
        mappings.put("Double", "FLOAT");
        mappings.put("java.lang.Double", "FLOAT");
        mappings.put("float", "FLOAT");
        mappings.put("Float", "FLOAT");
        mappings.put("java.lang.Float", "FLOAT");
        mappings.put("BigDecimal", "NUMBER(30,5)");
        mappings.put("java.math.BigDecimal", "NUMBER(30,5)");
        mappings.put("BigInteger", "NUMBER");
        mappings.put("java.math.BigInteger", "NUMBER");
        mappings.put("Time","TIME");
        mappings.put("java.sql.Time","TIME");
        mappings.put("Timestamp","TIMESTAMP_LTZ");
        mappings.put("java.sql.Timestamp","TIMESTAMP_LTZ");
    }

    private String getSQLType(String type) {
        String mappedType = mappings.get(type);
        if (mappedType == null) {
            return "VARCHAR";
        }
        return mappedType;
    }

    public String getArgsAndTypesSF() {
        StringBuilder sb = new StringBuilder();
        ConstructorInfo firstConstructor = constructors.get(0);
        for (int i = 0; i < firstConstructor.parameters.size(); i++) {
            ParamInfo param = firstConstructor.parameters.get(i);
            sb.append(param.paramName).append(" ").append(getSQLType(param.paramType));
            if (i < firstConstructor.parameters.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }


    public String getArgsAndTypes() {
        StringBuilder sb = new StringBuilder();
        ConstructorInfo firstConstructor = constructors.get(0);
        for (int i = 0; i < firstConstructor.parameters.size(); i++) {
            ParamInfo param = firstConstructor.parameters.get(i);
            String paramType = param.paramType;
            if (paramType.equals("java.util.Date")) {
                paramType = "java.sql.Date";
            }
            sb.append(paramType).append(" ").append( param.paramName);
            if (i < firstConstructor.parameters.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getArgs() {
        StringBuilder sb = new StringBuilder();
        ConstructorInfo firstConstructor = constructors.get(0);
        for (int i = 0; i < firstConstructor.parameters.size(); i++) {
            ParamInfo param = firstConstructor.parameters.get(i);
            sb.append(param.paramName);
            if (i < firstConstructor.parameters.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}