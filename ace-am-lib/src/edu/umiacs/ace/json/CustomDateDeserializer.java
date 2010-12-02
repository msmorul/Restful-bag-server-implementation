/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.json;

import java.io.IOException;
import java.util.Date;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

/**
 * 
 * @author toaster
 */
public class CustomDateDeserializer extends JsonDeserializer<Date>
{

    @Override
    public Date deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException,
            JsonProcessingException
    {
        try
        {
            return arg1.parseDate(arg0.getText());
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }

    }

}
