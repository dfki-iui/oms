function run()
  current = tonumber(dfki.getPayload("watchedBlock"))
  print("Current value is "..current)
  if current > 9000 then
    print("Sending an EMail about the filter being due.")
    dfki.mail("marc.mueller@dfki.de", "ADOMe warning", "The filter is due.")
  end
end
