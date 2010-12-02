/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.json;

import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

/**
 * 
 * @author toaster
 */
public class CustomBooleanDeserializer extends JsonDeserializer<Boolean>
{

    @Override
    public Boolean deserialize(JsonParser arg0, DeserializationContext arg1)
            throws IOException,
            JsonProcessingException
    {

        if (Strings.isEmpty(arg0.getText()))
        {
            return false;
        }
        return Boolean.parseBoolean(arg0.getText());

    }
}
