import io.edifact.routing.spi.rcv.impl.EdifactRouter;
import io.edifact.routing.spi.rcv.ksc.ext.EdifactRoutingKeyExtractor;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingKey;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingRule;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingRuleLoader;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public final class RoutingTest3 {


	public static void main(String[] args) throws Exception {
		
		String dir = "C:\\Program Files\\AnyLink7_Studio\\workspace\\edifact-routing-test\\resources";
		String ruleFileName = "edifact-routes.rules";
		
		String dirMsg = dir+File.separatorChar + "pbif00105";
		
		File ruleFile = new File(dir,ruleFileName);
		File msgFileDirectory = new File(dirMsg);
		File[] msgFiles = msgFileDirectory.listFiles();
		
		for(File msgFile : msgFiles){
			
			byte[] rulesBytes = Files.readAllBytes(ruleFile.toPath());
			byte[] msgBytes = Files.readAllBytes(msgFile.toPath());
			
			//final String msg = "UNB+SENDER+RECIPIENT'UNH+1+CUSINF:S:93A:KE'BGM+5IJ+123'" + "DOC+AA'DOC+BB'NAD+CZ+ABC'RFF+AFB:26NSSLG062I'UNT+7+1'UNZ+1+1'";
			String msg = new String(msgBytes);
			
			final List<EdifactRoutingRule> rules = EdifactRoutingRuleLoader.load(new ByteArrayInputStream(rulesBytes));
			System.out.println("RULE COUNT : " + rules.size());

			for (EdifactRoutingRule rule : rules) {
			    System.out.println(
			        "INTERFACE : " + rule.getInterfaceItemId()
			        + ", KEY : " + rule.getRoutingKey()
			    );
			}
			EdifactRouter edifactRouter = new EdifactRouter(rules);
			
			try{
				EdifactRoutingRule resolve1 = edifactRouter.resolve(msg);
				String interfaceItemId1 = resolve1.getInterfaceItemId();
				
				/* or 
				EdifactRoutingRule resolve2 = edifactRouter.resolve(key);
				String interfaceItemId2 = resolve2.getInterfaceItemId();
				System.out.println(interfaceItemId2);
				*/
				
				System.out.println(interfaceItemId1);
			}
			catch(IllegalStateException e){
				e.printStackTrace();
			}
			
		}
		
	}
	
	public String userClassFuntionGetInterfaceId (File ruleFile , File msgFile ) throws IOException {
		
		byte[] rulesBytes = Files.readAllBytes(ruleFile.toPath());
			
		byte[] msgBytes = Files.readAllBytes(msgFile.toPath());
		
		String msg = new String(msgBytes);
		
		final List<EdifactRoutingRule> loaded = EdifactRoutingRuleLoader.load(new ByteArrayInputStream(rulesBytes));
		
		EdifactRouter edifactRouter = new EdifactRouter(loaded);
		
		try{
			
			EdifactRoutingRule resolve1 = edifactRouter.resolve(msg);
			String interfaceItemId1 = resolve1.getInterfaceItemId();
			System.out.println(interfaceItemId1);
			return interfaceItemId1;
			/* or 
			EdifactRoutingRule resolve2 = edifactRouter.resolve(key);
			String interfaceItemId2 = resolve2.getInterfaceItemId();
			System.out.println(interfaceItemId2);
			return interfaceItemId2;
			*/
		}
		catch(IllegalStateException e){
			e.printStackTrace();
		}
		
		return null;
	}
	
}