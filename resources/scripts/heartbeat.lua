-- This segment of code defines a function called checkTemperatures that expects three arguments.
-- The function itself iterates over each block ID in the given table, retrieves its
-- payload via the dfki.getPayload(id) function and compares it to the thresholds passed as
-- second and third arguments. If the value is within the given bounds, the block with the
-- current ID is deleted using the dfki.deleteBlock(id) function.
function checkTemperatures(blocks, min, max)
	-- Iterate over the table with i being the key and v the value of the current entry.
	for i, v in ipairs(blocks) do
		-- Read the temperature as a number from the payload of the block with the current id.
		temperature = tonumber(dfki.getPayload(v))

		-- Compare the retrieved temperature 
		-- Remark: Since this script is contained inside an XML document, the
		-- 'less-than' and 'greater-than' signs need to be escaped accordingly,
		-- so as not to cause parsing errors.
		if (temperature > min and temperature < max) then
			-- Delete the block since it contains ordinary temperatures.
			dfki.deleteBlock(v)
			return
		end
	end
end

-- The run function is called from the server to start a Lua script. In this case, the run-
-- function retrieves every block ID and filters them to only contain block IDs in the 
-- temperature name space. After that, temperature thresholds are read from fixed blocks and 
-- passed to the checkTemperature-function along with the relevant block IDs.
function run()
	-- Get every block ID from the dfki.getBlockIDs-function.
	allBlocks = dfki.getBlockIDs()
	-- Filter out all blocks not inside the urn:adome:example:temperature name space
	relevantBlocks = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:temperature")
	
	minTemperatureBlocks = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:lowerThreshold")
	maxTemperatureBlocks = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:upperThreshold")
	-- Read the payload of the lowerThreshold-block and interpret it as a number 
	minTemperature = tonumber(dfki.getPayload(minTemperatureBlocks[next(minTemperatureBlocks)]))
	-- Read the payload of the upperThreshold-block and interpret it as a number
	maxTemperature = tonumber(dfki.getPayload(maxTemperatureBlocks[next(maxTemperatureBlocks)]))
	
	-- Let the checkTemperature-function delete blocks inside the given min-max-range
	checkTemperatures(relevantBlocks, minTemperature, maxTemperature)
end