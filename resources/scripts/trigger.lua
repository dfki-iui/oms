function sendMail()
	allBlocks = dfki.getBlockIDs()
	print("Sending an EMail about the system overheating.")
	messageBlocks = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:warning")
	message = dfki.getPayload(messageBlocks[next(messageBlocks)])
	
	recipients = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:recipients")
	for i, v in ipairs(recipients) do
		dfki.mail(dfki.getPayload(v), "ADOMe warning.", message)
	end
	
	dfki.postPayload("emailindicator", 1)
end
		
-- This run-function is supposed for the trigger task that gets called each time the watched
-- block is updated. It will react to a change in the block by reading its payload and testing
-- if the temperature exceeds the fixed value of 50. If so, an EMail is sent warning the user
-- of the overheating.
function run()
	allBlocks = dfki.getBlockIDs()
	thresholdBlocks = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:threshold");
	threshold = tonumber(dfki.getPayload(thresholdBlocks[next(thresholdBlocks)]))
		
	-- Retrieve the current payload of the watched block as a number
	current = tonumber(dfki.getPayload("watchedBlock"))
	print("Current temperature is: "..tostring(current))
	
	-- Check if the temperature is greather than 50 and no EMail was sent before.
	if (current > threshold and tonumber(dfki.getPayload("emailindicator")) == 0) then
		sendMail()
	end
end
