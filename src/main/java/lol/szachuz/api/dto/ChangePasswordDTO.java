package lol.szachuz.api.dto;

import jakarta.json.bind.annotation.JsonbProperty;

public class ChangePasswordDTO {
    @JsonbProperty("oldPassword")
    public String oldPassword;

    @JsonbProperty("newPassword")
    public String newPassword;
}
