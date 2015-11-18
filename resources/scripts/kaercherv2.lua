function install()
  dfki.deleteBlock("1")
  threshold = {
    namespace = "urn:adome:example:threshold",
    titles = { { lang = "en", text = "Threshold block" } },
    descriptions = { en = "This block contains a threshold value." },
    ["format"] = "text/plain",
    ["type"] = "http://purl.org/dc/dcmitype/Text",
    payload = "12000"
  }
  print("Posting threshold block.")
  dfki.postBlock(threshold)
  
  indicator = {
    namespace = "urn:adome:example:emailindicator",
    titles = { en = "EMail indicator" },
    descriptions = { en = "This block stores a flag for whether or not an email has been sent." },
    ["format"] = "text/plain",
    ["type"] = "http://purl.org/dc/dcmitype/Text",
    payload = "0"
  }
  print("Posting indicator block.")
  dfki.postBlock(indicator)
end

function run()
  allBlocks = dfki.getBlockIDs()
  thresholdBlocks = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:threshold");
  threshold = tonumber(dfki.getPayload(thresholdBlocks[next(thresholdBlocks)]))

  current = tonumber(dfki.getPayload("watchedBlock"))
  
  if current > threshold then
    indicators = dfki.nameSpaceFilter(allBlocks, "urn:adome:example:emailindicator")
    indicator = indicators[next(indicators)]
    sent = tonumber(dfki.getPayload(indicator))
    
    if sent ~= 1 then
      print("Sending an EMail about the filter being due.")
      dfki.mail("marc.mueller@dfki.de", "ADOMe warning", "The filter is due.")
      dfki.postPayload(indicator, 1)
    end
  end
end
