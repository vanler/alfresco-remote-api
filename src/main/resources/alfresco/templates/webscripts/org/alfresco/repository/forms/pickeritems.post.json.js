<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/repository/forms/pickerresults.lib.js">

function main()
{
   var count = 0,
      items = [],
      results = [];

   // extract mandatory data from request body
   if (!json.has("items"))
   {
       status.setCode(status.STATUS_BAD_REQUEST, "items parameter is not present");
       return;
   }
   
   // convert the JSONArray object into a native JavaScript array
   var jsonItems = json.get("items"),
      itemValueType = "nodeRef",
      itemValueTypeHint = "",
      numItems = jsonItems.size(),
      item, result;
   
   if (json.has("itemValueType"))
   {
      var jsonValueTypes = json.get("itemValueType").textValue().split(";");
      itemValueType = jsonValueTypes[0];
      itemValueTypeHint = (jsonValueTypes.length > 1) ? jsonValueTypes[1] : "";
   }
   
   for (count = 0; count < numItems; count++)
   {
      item = jsonItems.get(count);
      var container = null;
      if (item.textValue() != "")
      {
         result = null;
         if (itemValueType == "nodeRef")
         {
            result = search.findNode(item.textValue());
            if (result)
            {
               var qnamePaths = result.qnamePath.split("/");
               if ((qnamePaths.length > 4) && (qnamePaths[2] == "st:sites"))
               {
                  container = qnamePaths[4].substr(3);
               }
            }
         }
         else if (itemValueType == "xpath")
         {
            result = search.xpathSearch(itemValueTypeHint.replace("%VALUE%", search.ISO9075Encode(item.textValue())))[0];
         }
         
         if (result != null)
         {
            // create a separate object if the node represents a user or group
            if (result.isSubType("cm:person"))
            {
               result = createPersonResult(result);
            }
            else if (result.isSubType("cm:authorityContainer"))
            {
               result = createGroupResult(result);
            }
            
            results.push(
            {
               item: result,
               container: container
            });
         }
      }
   }

   if (logger.isLoggingEnabled())
       logger.log("#items = " + count + ", #results = " + results.length);

   model.results = results;
}

main();
