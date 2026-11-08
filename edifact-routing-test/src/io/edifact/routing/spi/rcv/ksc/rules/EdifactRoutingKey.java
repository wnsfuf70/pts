package io.edifact.routing.spi.rcv.ksc.rules;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class EdifactRoutingKey {
	
    private final String edifactType;
    private final String bgmCode;
    private final Set<String> docCodes;

    public EdifactRoutingKey(String edifactType, String bgmCode, Collection<String> docCodes) {
    	
        if (edifactType == null || edifactType.trim().length() == 0) {
            throw new IllegalArgumentException("edifactType is required");
        }
        
        this.edifactType = edifactType.trim();
        this.bgmCode = bgmCode == null ? null : bgmCode.trim();
        LinkedHashSet<String> copy = new LinkedHashSet<String>();
        
        if (docCodes != null) {
            for (String docCode : docCodes) {
                if (docCode != null && docCode.trim().length() > 0) {
                    copy.add(docCode.trim());
                }
            }
        }
        
        this.docCodes = Collections.unmodifiableSet(copy);
    }

    public String getEdifactType() { 
    	return edifactType; 
    }
    
    public String getBgmCode() { 
    	return bgmCode; 
    }
    
    public Set<String> getDocCodes() { 
    	return docCodes; 
    }

    @Override
    public String toString() {
        return "type=" + edifactType + ", bgm=" + bgmCode + ", docs=" + docCodes;
    }
    
}