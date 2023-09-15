package az.atl.msauth.feign;


import az.atl.msauth.dto.request.message.*;
import az.atl.msauth.dto.response.message.*;
import org.bouncycastle.oer.Switch;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "feignMessage", url = "http://localhost:8085/omnio")
public interface MessageFeignClient {

    @PostMapping("/messaging/send")
    ResponseEntity<DeliverResponse> privateMessage
            (
                    @RequestHeader(name = "Authorization") String header,
                    @RequestBody ClientPrivateMessageRequest request
            );


    // When registering, we save the user in the user table
    @PostMapping("/auth/save")
    ResponseEntity<ClientSaveResponse> saveUser
    (
            @RequestBody ClientSaveRequest request
    );

    // Upon successful authentication, we change the status icon to ONLINE

    @PostMapping("/status/switch")
    ResponseEntity<ClientSaveResponse> switchStatus(
            @RequestBody SwitchStatusRequest request
    );


    // FRIENDSHIP CONTROLLER
    // Send a friendship invitation(status PENDING)
    @PostMapping("/friendship/send")
    ResponseEntity<FriendShipResponse> sendFriendShip(
            @RequestHeader(name = "Authorization") String token,
            @RequestHeader(name = "Accept-Language",required = false)String lang,
            @RequestBody FriendShipRequest request
    );

    @PostMapping("/friendship/accept")
    ResponseEntity<FriendShipResponse> acceptFriendship(
            @RequestHeader(name = "Authorization") String token,
            @RequestHeader(name = "Accept-Language",required = false)String lang,
            @RequestBody AcceptFriendShipRequest request
    );

    @DeleteMapping("/friendship/reject/{username}")
    ResponseEntity<FriendShipResponse> rejectFriendRequest(
            @RequestHeader(name = "Authorization") String token,
            @RequestHeader(name = "Accept-Language",required = false)String lang,
            @PathVariable(name = "username") String username
    );
    @GetMapping("/friendship/friendList")
    ResponseEntity<List<FriendListResponse>> friendList(
            @RequestHeader(name = "Authorization") String token,
            @RequestHeader(name = "Accept-Language",required = false)String lang
    );

    @GetMapping("/friendship/pending")
    ResponseEntity<List<AcceptFriendShipResponse>> getPendingUsers(
            @RequestHeader(name = "Authorization") String token,
            @RequestHeader(name = "Accept-Language",required = false)String lang
    );

    @DeleteMapping("/friendship/block/{username}")
    ResponseEntity<FriendShipResponse> blockUser(
            @RequestHeader(name = "Authorization") String token,
            @RequestHeader(name = "Accept-Language",required = false)String lang,
            @PathVariable(name = "username")String username
    );

    @PostMapping("/friendship/unblock/{username}")
    ResponseEntity<FriendShipResponse> unblockUser(
            @RequestHeader(name = "Authorization") String header,
            @RequestHeader(name = "Accept-Language",required = false)String lang,
            @PathVariable(name = "username") String username
    );

    // MESSAGE CONTROLLER

    @PostMapping("/messaging/send")
    ResponseEntity<DeliverResponse> sendMessage(
            @RequestHeader(name = "Authorization") String header,
            @RequestHeader(name = "Accept-Language",required = false) String lang,
            @RequestBody MessageRequest message
    );

    @GetMapping("/messaging/getMessages/{username}")
    ResponseEntity<List<MessageResponse>> getMessages(
            @RequestHeader(name = "Authorization") String header,
            @RequestHeader(name = "Accept-Language",required = false) String lang,
            @PathVariable(name = "username")String username
    );

    @GetMapping("/messaging/getChats")
    ResponseEntity<List<ChatListResponse>> getChats(
            @RequestHeader(name = "Authorization")String header,
            @RequestHeader(name = "Accept-Language",required = false) String lang
            );

    // REPORT CONTROLLER

    @GetMapping("/activity/report")
    ResponseEntity<ActivityReportResponse> getReport(
            @RequestHeader(name = "Authorization")String token,
            @RequestHeader(name = "Accept-Language",required = false)String lang
    );
}