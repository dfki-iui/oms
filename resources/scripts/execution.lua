-- This bit of code defines a Lua function called checkRange that expects a table
-- of block IDs as its first argument and min/max thresholds as second and third.
-- The function itself iterates over each key-value pair of the table (in this
-- case each key is the index of each entry), retrieves the payload of each block
-- and tests if the block's value is within the given bounds. This function returns
-- true iff all payload values are inside the given range.
function checkRange(blocks, min, max)
	-- Iterate over each key-value pair, i being the index and v the value in the table.
	for i, v in ipairs(blocks) do
		-- Get the payload of the block with the current ID
		reading = tonumber(dfki.getPayload(v))
		-- Check if the received value is within range
		-- Remark: Since this script is contained inside an XML document, the
		-- 'less-than' and 'greater-than' signs need to be escaped accordingly,
		-- so as not to cause parsing errors.
		if (reading < min or reading > max) then
			-- Returning inside the loop will stop the function execution immediately.
			return false
		end
	end
	-- This return statement is only reached when each and every reading was within range.
	return true
end

-- The run function is called from the server to start a Lua script. In this case, the run-
-- function retrieves every block ID and filters them twice. Once to retrieve every humidity
-- reading and again to find every temperature reading. Both tables of block IDs are then
-- checked against certain threshold read from the respective blocks. The result of both
-- checks is returned as a string for display on the web page.
function run()
	-- Get every block ID from the dfki.getBlockIDs-function.
	allBlocks = dfki.getBlockIDs()

	-- Filter out all blocks not inside the urn:adome:example:humidity name space
	relevantHumidityBlocks = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:humidity")
	-- Read threshold values from blocks.		
	minHumidityBlocks = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:lowerHumidityThreshold")
	maxHumidityBlocks = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:upperHumidityThreshold")
	print(minHumidityBlocks)
	minHumidity = tonumber(dfki.getPayload(minHumidityBlocks[next(minHumidityBlocks)]))
	maxHumidity = tonumber(dfki.getPayload(maxHumidityBlocks[next(maxHumidityBlocks)]))
	
	-- Filter out all blocks not inside the urn:adome:example:humidity name space
	relevantTemperatureBlocks = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:temperature")
	-- Read threshold values from blocks.
	minTemperatureBlocks = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:lowerTemperatureThreshold")
	maxTemperatureBlocks = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:upperTemperatureThreshold")
	minTemperature = tonumber(dfki.getPayload(minTemperatureBlocks[next(minTemperatureBlocks)]))
	maxTemperature = tonumber(dfki.getPayload(maxTemperatureBlocks[next(maxTemperatureBlocks)]))
	
	-- Use checkRange to perform a range test for both humidity and temperature blocks.
	humidityOkay = checkRange(relevantHumidityBlocks, minHumidity, maxHumidity)
	temperatureOkay = checkRange(relevantTemperatureBlocks, minTemperature, maxTemperature)
	
	-- Create an empty string for the return message.
	ret = ""
	-- Append a message indicating the result of the humidity test to the string.
	if (humidityOkay) then
		ret = ret.."Humidity values were all fine. "
	else
		ret = ret.."Humidity values were out of range. "
	end

	ret = ret.."<br />"

	-- Append a message indicating the result of the temperature test to the string.
	if (temperatureOkay) then
		ret = ret.."Temperature values were all fine. "
	else
		ret = ret.."Temperature values were out of range. "
	end
	
	-- Return the result to ADOM's Lua interpreter.
	return ret
end