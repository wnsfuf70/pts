
import io.edifact.routing.spi.core.EdifactParser;
import io.edifact.routing.spi.rcv.impl.EdifactRouter;
import io.edifact.routing.spi.message.EdifactMessage;
import io.edifact.routing.spi.rcv.ksc.condition.FieldCondition;
import io.edifact.routing.spi.rcv.ksc.ext.EdifactRoutingKeyExtractor;
import io.edifact.routing.spi.rcv.ksc.ext.RoutingFieldExtractor;
import io.edifact.routing.spi.rcv.ksc.ext.SegmentFieldExtractor;
import io.edifact.routing.spi.rcv.ksc.ext.StandardFieldExtractor;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingKey;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingRule;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingRuleLoader;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public final class RoutingTest {

	private static int passed = 0;

	private static void eq(Object expected, Object actual) {
		if (!Objects.equals(expected, actual))
			throw new AssertionError("expected=" + expected + " actual="
					+ actual);
		passed++;
	}

	private static void fails(Runnable task, String substring) {
		try {
			task.run();
		} catch (RuntimeException ex) {
			if (ex.getMessage() == null || !ex.getMessage().contains(substring))
				throw new AssertionError(ex);
			passed++;
			return;
		}
		throw new AssertionError("Expected error: " + substring);
	}

	public static void main(String[] args) throws Exception {
		
		final String msg = "UNB+SENDER+RECIPIENT'UNH+1+CUSINF:S:93A:KE'BGM+5IJ+123'"
				+ "DOC+AA'DOC+BB'NAD+CZ+ABC'RFF+AFB:26NSSLG062I'UNT+7+1'UNZ+1+1'";
		final EdifactRoutingKey key = EdifactRoutingKeyExtractor.extract(msg);
		
		eq("CUSINF93A", key.getEdifactType());
		eq("5IJ", key.getBgmCode());
		eq(new LinkedHashSet<String>(Arrays.asList("AA", "BB")),key.getDocCodes());
		
		List<String> rulesList = Files.readAllLines(Paths.get("C:\\Program Files\\AnyLink7_Studio\\workspace\\edifact-routing-test\\resources\\edifact-routes.rules"), Charset.defaultCharset());
		String rules = "# id|type|bgm|docs|extra conditions\n"
				+ "OTHER|CUSINF93A|5IJ|ZZ\n"
				+ "TARGET|CUSINF93A|5IJ|BB|NAD.1.0~EQ~CZ;RFF.1.1~EQ~26NSSLG062I\n";
		
		final List<EdifactRoutingRule> loaded = EdifactRoutingRuleLoader.load(
				new ByteArrayInputStream(rules.getBytes(StandardCharsets.UTF_8))
			);
		
		String interfaceItemId = new EdifactRouter(loaded).resolve(msg).getInterfaceItemId();
		System.out.println(interfaceItemId);
		
		eq("TARGET", new EdifactRouter(loaded).resolve(msg)
				.getInterfaceItemId());
		fails(new Runnable() {
			@Override
			public void run() {
				new EdifactRouter(loaded).resolve(key);
			}
		}, "requires full EDIFACT routing context");

		final String altered = msg.replace("NAD+CZ", "NAD+CN");

		fails(new Runnable() {
			@Override
			public void run() {
				new EdifactRouter(loaded).resolve(altered);
			}
		}, "No EDIFACT routing rule");

		String escaped = "UNA:+.? 'UNH+1+CUSINF:S:93A:KE'BGM+5IJ'DOC+AA'FTX+AAA+++Hello?+World'";
		EdifactMessage parsed = EdifactParser.parse(escaped);
		
		eq("Hello+World", parsed.segments("FTX").get(0).value(4, 0));
		String duplicate = "R1|CUSINF93A|5IJ|*\nR2|CUSINF93A|*|BB\n";
		
		final List<EdifactRoutingRule> duplicateRules = EdifactRoutingRuleLoader
				.load(new ByteArrayInputStream(duplicate
				.getBytes(StandardCharsets.UTF_8)));
		
		fails(new Runnable() {
			@Override
			public void run() {
				new EdifactRouter(duplicateRules).resolve(msg);
			}
		}, "Ambiguous EDIFACT routing");

		RoutingFieldExtractor<List<String>> custom = new RoutingFieldExtractor<List<String>>() {
			
			public boolean supports(String field) {
				return "CUSTOM.SENDER".equals(field);
			}

			public List<String> extract(EdifactMessage m, String field) {
				return Collections.singletonList("partner-x");
			}
			
		};
		
		List<RoutingFieldExtractor<List<String>>> extractors = Arrays.asList(new StandardFieldExtractor(), new SegmentFieldExtractor(),custom);
		EdifactRoutingRule customRule = new EdifactRoutingRule("CUSTOM",
				new EdifactRoutingKey("CUSINF93A", null, null),
				new FieldCondition("CUSTOM.SENDER", FieldCondition.Operator.EQ,"partner-x")
		);
		eq("CUSTOM", new EdifactRouter(Collections.singletonList(customRule),extractors).resolve(msg).getInterfaceItemId());
		System.out.println("PASSED " + passed + " checks");
		
	}

}