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
public class CustomCharDeserializer extends JsonDeserializer<Character>
{

    @Override
    public Character deserialize(JsonParser arg0, DeserializationContext arg1)
            throws IOException,
            JsonProcessingException
    {

        if (Strings.isEmpty(arg0.getText()))
        {
            return Character.MIN_VALUE;
        }
        return arg0.getText().charAt(0);

    }
}
