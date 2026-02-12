package io.shiftleft.controller;

@RequestMapping(value = "/saveSettings", method = RequestMethod.GET)
  public void saveSettings(HttpServletResponse httpResponse, WebRequest request) throws Exception {
    // "Settings" will be stored in a cookie
    // schema: base64(filename,value1,value2...), md5sum(base64(filename,value1,value2...))

    if (!checkCookie(request)){
      httpResponse.getOutputStream().println("Error");
      throw new Exception("cookie is incorrect");
    }

    String settingsCookie = request.getHeader("Cookie");
    String[] cookie = settingsCookie.split(",");
if(cookie.length<2) {
  httpResponse.getOutputStream().println("Malformed cookie");
      throw new Exception("cookie is incorrect");
    }

    String base64txt = cookie[0].replace("settings=","");

    // Check md5sum
@RequestMapping(value = "/saveSettings", method = RequestMethod.GET)
    public void saveSettings(HttpServletResponse httpResponse, WebRequest request) throws Exception {
        if (!checkCookie(request)) {
            httpResponse.getOutputStream().println("Error");
            throw new Exception("cookie is incorrect");
        }

        String settingsCookie = request.getHeader("Cookie");
        String[] cookie = settingsCookie.split(",");
        if (cookie.length < 2) {
            httpResponse.getOutputStream().println("Malformed cookie");
            throw new Exception("cookie is incorrect");
        }

        String base64txt = cookie[0].replace("settings=", "");
        String cookieMD5sum = cookie[1];
        String calcMD5Sum = DigestUtils.md5Hex(base64txt);

        if (!cookieMD5sum.equals(calcMD5Sum)) {
            httpResponse.getOutputStream().println("Wrong md5");
            throw new Exception("Invalid MD5");
        }

        String[] settings = new String(Base64.getDecoder().decode(base64txt)).split(",");
        ClassPathResource cpr = new ClassPathResource("./static/");
        
        // SECURITY FIX: Instead of using user-provided filename, generate a secure filename
        // Option 1: Generate UUID-based filename
        String userIdentifier = getUserIdentifier(request);
        String secureFilename = UUID.randomUUID().toString() + ".settings";
        
        // Option 2: Content-addressed storage approach
        String[] settingsArr = Arrays.copyOfRange(settings, 1, settings.length);
        String settingsContent = String.join("\n", settingsArr);
        String contentHash = DigestUtils.sha256Hex(settingsContent);
        String contentAddressedFilename = contentHash + ".settings";
        
        // Use the content-addressed filename for stronger security
        Path storageRoot = Paths.get(cpr.getPath()).normalize();
        Path secureStoragePath = storageRoot.resolve(contentAddressedFilename).normalize();
        File file = secureStoragePath.toFile();
        
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        // Write the settings content to the secure file location
        FileOutputStream fos = new FileOutputStream(file, true);
        fos.write(settingsContent.getBytes());
        fos.write(("\n" + cookie[cookie.length - 1]).getBytes());
        fos.close();
        
        // Store mapping for future reference (in production, this would save to database)
        storeUserSettingsMapping(userIdentifier, contentAddressedFilename);
        
        httpResponse.getOutputStream().println("Settings Saved");
    }
    
    // Helper method to get user identifier from the request
    private String getUserIdentifier(WebRequest request) {
        // In a real implementation, this would extract authenticated user information
        // For example: return userService.getCurrentUserId(request);
        return request.getRemoteUser() != null ? request.getRemoteUser() : "anonymous";
    }
    
    // Helper method to store user-to-filename mapping
    private void storeUserSettingsMapping(String userIdentifier, String filename) {
        // In a real implementation, this would store the mapping in a database
        // For example: userSettingsRepository.saveMapping(userIdentifier, filename);
        System.out.println("Stored mapping: " + userIdentifier + " -> " + filename);
    }
