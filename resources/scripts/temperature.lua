-- The run-function defined in these lines is called by the server when a Lua task is executed.
-- This function is supposed to simulate a rising temperature that is written to the block
-- with the id "watchedBlock". This is in place of a sensor reading a rising temperature,
-- causing the script watching the "watchedBlock" being triggered.

function run()
	-- Retrieve the payload of the target block as a number.
	current = dfki.getPayload("watchedBlock")
	sent = tonumber(dfki.getPayload("emailindicator"))
	if (sent == 0) then
		print("Simulating newly read temperature: "..tostring(current + 6))
		-- Increment and store the new value in the payload of the watched block.
		dfki.postPayload("watchedBlock", current + 6)	
	end	
end
