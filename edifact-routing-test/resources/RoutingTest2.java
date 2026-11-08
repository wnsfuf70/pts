import io.edifact.routing.spi.rcv.impl.EdifactRouter;
import io.edifact.routing.spi.rcv.ksc.ext.EdifactRoutingKeyExtractor;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingKey;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingRule;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingRuleLoader;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public final class RoutingTest2 {


	public static void main(String[] args) throws Exception {
		
		String dir = "C:\\Program Files\\AnyLink7_Studio\\workspace\\edifact-routing-test\\resources";
		String ruleFileName = "edifact-routes.rules";
		
		String dirMsg = dir+File.separatorChar + "pbif00101";
		String msgFileName = "Copy of PBIF00101_E5E1W8Z16401z";
		
		File ruleFile = new File(dir,ruleFileName);
		File msgFile = new File(dirMsg,msgFileName);
		byte[] rulesBytes = Files.readAllBytes(ruleFile.toPath());
		byte[] msgBytes = Files.readAllBytes(msgFile.toPath());
		
		//final String msg = "UNB+SENDER+RECIPIENT'UNH+1+CUSINF:S:93A:KE'BGM+5IJ+123'" + "DOC+AA'DOC+BB'NAD+CZ+ABC'RFF+AFB:26NSSLG062I'UNT+7+1'UNZ+1+1'";
		String msg = new String(msgBytes);
		
		final List<EdifactRoutingRule> loaded = EdifactRoutingRuleLoader.load(new ByteArrayInputStream(rulesBytes));
		
		final EdifactRoutingKey key = EdifactRoutingKeyExtractor.extract(msg);
		
		EdifactRouter edifactRouter = new EdifactRouter(loaded);
		EdifactRoutingRule resolve = edifactRouter.resolve(msg);
		
		String interfaceItemId = edifactRouter.resolve(key).getInterfaceItemId();
		
		String interfaceItemId2 = edifactRouter.resolve(msg).getInterfaceItemId();
		System.out.println(interfaceItemId);
		System.out.println(interfaceItemId2);
		
	}

}