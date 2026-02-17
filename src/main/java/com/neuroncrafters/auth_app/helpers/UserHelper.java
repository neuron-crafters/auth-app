package com.neuroncrafters.auth_app.helpers;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class UserHelper {
    public static UUID parseUUID(String uuid) {
        return UUID.fromString(uuid);
    }
}
