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
public class CustomLongDeserializer extends JsonDeserializer<Long>
{

    @Override
    public Long deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException,
            JsonProcessingException
    {
        try
        {
            if (Strings.isEmpty(arg1))
                return (long)-1;
            return Long.parseLong(arg0.getText());
        }
        catch (NumberFormatException e)
        {
            return (long)-1;
        }

    }

}
