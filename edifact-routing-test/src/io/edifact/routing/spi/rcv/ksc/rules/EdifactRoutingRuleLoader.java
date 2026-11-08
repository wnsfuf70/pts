package io.edifact.routing.spi.rcv.ksc.rules;

import io.edifact.routing.spi.rcv.ksc.condition.AndCondition;
import io.edifact.routing.spi.rcv.ksc.condition.FieldCondition;
import io.edifact.routing.spi.rcv.ksc.condition.RoutingCondition;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class EdifactRoutingRuleLoader {
	
	private EdifactRoutingRuleLoader() { }

	public static List<EdifactRoutingRule> load(List<String> lines) throws IOException {

	    if (lines == null) {
	        throw new IllegalArgumentException("rule lines is null");
	    }

	    return parse(lines);
	}
	
	public static List<EdifactRoutingRule> load(Map<String, String> ruleMap) throws IOException {

	    if (ruleMap == null) {
	        throw new IllegalArgumentException("rule map is null");
	    }

	    List<String> lines = new ArrayList<String>(ruleMap.size());

	    for (Map.Entry<String, String> entry : ruleMap.entrySet()) {

	        String interfaceItemId = entry.getKey();
	        String rule = entry.getValue();

	        if (interfaceItemId == null || interfaceItemId.trim().isEmpty()) {
	            throw new IllegalArgumentException(
	                "interface item id is required"
	            );
	        }

	        if (rule == null || rule.trim().isEmpty()) {
	            throw new IllegalArgumentException(
	                "rule is required for " + interfaceItemId
	            );
	        }

	        lines.add(interfaceItemId.trim() + "|" + rule.trim());
	    }

	    return parse(lines);
	}
	
	public static List<EdifactRoutingRule> load(InputStream stream) throws IOException {
	    return load(stream, false);
	}

	public static List<EdifactRoutingRule> load(InputStream stream, boolean ioClose) throws IOException {

	    if (stream == null) {
	        throw new IllegalArgumentException("rule stream is null");
	    }

	    BufferedReader reader = new BufferedReader(
	        new InputStreamReader(stream, StandardCharsets.UTF_8)
	    );

	    try {
	        List<String> lines = new ArrayList<String>();

	        String line;
	        while ((line = reader.readLine()) != null) {
	            lines.add(line);
	        }

	        return parse(lines);

	    } finally {
	        if (ioClose) {
	            reader.close();
	        }
	    }
	}
	
	private static List<EdifactRoutingRule> parse(Iterable<String> lines) throws IOException {

	    List<EdifactRoutingRule> rules = new ArrayList<EdifactRoutingRule>();

	    int number = 0;

	    for (String line : lines) {

	        number++;

	        if (line == null) {
	            throw new IOException("Null rule at line " + number);
	        }

	        line = line.trim();

	        if (line.isEmpty() || line.startsWith("#")) {
	            continue;
	        }

	        String[] c = line.split("\\|", -1);

	        if (c.length != 4 && c.length != 5) {
	            throw new IOException(
	                "Invalid rule columns at line " + number + ": " + line
	            );
	        }

	        try {
	            String bgm = optional(c[2]);
	            String docs = optional(c[3]);

	            Collection<String> docCodes = docs == null
	                    ? Collections.<String>emptyList()
	                    : parseDocs(docs);

	            RoutingCondition extra = c.length == 5
	                    ? parseConditions(optional(c[4]))
	                    : null;

	            rules.add(new EdifactRoutingRule(
	                    c[0].trim(),
	                    new EdifactRoutingKey(c[1].trim(), bgm, docCodes),
	                    extra
	            ));

	        } catch (IllegalArgumentException ex) {
	            throw new IOException(
	                "Invalid rule at line " + number + ": " + ex.getMessage(),
	                ex
	            );
	        }
	    }

	    return Collections.unmodifiableList(rules);
	}
	
	private static String optional(String text) {
		text = text.trim();
		return text.isEmpty() || "*".equals(text) ? null : text;
	}

	private static Collection<String> parseDocs(String text) {
		List<String> docs = new ArrayList<String>();
		for (String item : text.split(",", -1)) {
			item = item.trim();
			if (item.isEmpty() || "*".equals(item)){
				throw new IllegalArgumentException("invalid DOC code");
			}
			docs.add(item);
		}
		return docs;
	}

	private static RoutingCondition parseConditions(String encoded) {
		
		if (encoded == null){
			return null;
		}
		
		List<RoutingCondition> conditions = new ArrayList<RoutingCondition>();
		
		for (String expression : encoded.split(";", -1)) {
			String[] bits = expression.trim().split("~", -1);
			if (bits.length != 3){
				throw new IllegalArgumentException("condition must be FIELD~OP~VALUE: " + expression);
			}
			FieldCondition.Operator operator = FieldCondition.Operator.valueOf(bits[1].trim());
			conditions.add(new FieldCondition(bits[0].trim(), operator, bits[2]));
		}
		
		return new AndCondition(conditions);
	}
	
}