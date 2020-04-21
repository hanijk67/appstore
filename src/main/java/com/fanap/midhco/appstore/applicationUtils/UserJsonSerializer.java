package com.fanap.midhco.appstore.applicationUtils;

import com.fanap.midhco.appstore.entities.Role;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.restControllers.vos.RoleVO;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by A.Moshiri on 3/26/2018.
 */
public class UserJsonSerializer extends JsonSerializer<User> {

    private static ThreadLocal<Integer> depth = new ThreadLocal<Integer>() {

        @Override
        protected Integer initialValue() {
            return 0;
        }

    };

    @Override
    public void serialize(User user, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        try {
            jgen.writeStartObject();
            if (user.getUserName() != null && !user.getUserName().equals("")) {
                jgen.writeStringField("userName", user.getUserName());
            }
            if (user.getFullName() != null && !user.getFullName().trim().equals("")) {
                jgen.writeStringField("fullName", user.getFullName());
            }
            jgen.writeStringField("firstName", user.getContact().getFirstName());
            jgen.writeStringField("lastName", user.getContact().getLastName());

            User createdByUser = user.getCreatorUser();
            if (createdByUser != null) {
                jgen.writeStringField(
                        "createdBy",
                        createdByUser.getContact().getFirstName() + " "
                                + createdByUser.getContact().getLastName());
            }
            if (user.getCreationDate() != null) {
                jgen.writeStringField("creationDate", String.valueOf(user.getCreationDate().getTime()));

            }
            User lastModifiedByUser = user.getLastModifyUser();
            if (lastModifiedByUser != null) {
                jgen.writeStringField("lastModifiedBy",
                        lastModifiedByUser.getContact().getFirstName() + " "
                                + lastModifiedByUser.getContact().getLastName());
            }
            if (user.getLastModifyDate() != null) {
                jgen.writeStringField("modifyDate", String.valueOf(user.getLastModifyDate().getTime()));
            }
            Long id = user.getId();

            if (id != null) {
                jgen.writeStringField("id", String.valueOf(id));
            }

            if (user.getUserId() != null) {
                jgen.writeStringField("userId", String.valueOf(user.getUserId()));
            }

            if (user.getUserStatus() != null) {
                jgen.writeStringField("userStatus", String.valueOf(user.getUserStatus().getStatus()));
            }

            if (user.getRoles() != null) {
                List<RoleVO> roleVOList = new ArrayList<>();
                Set<Role> roleSet =user.getRoles();
                List<Role> roleList = new ArrayList<>(roleSet);

                Stream<RoleVO> roleVOStream = roleList.stream().map(roleInList -> RoleVO.buildRoleVOByRole(roleInList));

                roleVOList = roleVOStream.collect(Collectors.<RoleVO>toList());
                jgen.writeStringField("roles ", JsonUtil.getJson(roleVOList));
            }

            if (user.getLastIp() != null && !user.getLastIp().trim().equals("")) {
                jgen.writeStringField("lastIp", user.getLastIp());
            }

            if (user.isLogged()) {
                jgen.writeStringField("isLogged", "true");
            } else {
                jgen.writeStringField("isLogged", "false");
            }

            jgen.writeEndObject();

            return;


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
