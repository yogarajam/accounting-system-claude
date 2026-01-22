package com.accounting.controller;

import com.accounting.model.User;
import com.accounting.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users/list";
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", userService.findAllRoles());
        return "users/form";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute User user,
                           @RequestParam String password,
                           @RequestParam String roleName,
                           RedirectAttributes redirectAttributes) {
        userService.createUser(user, password, roleName);
        redirectAttributes.addFlashAttribute("successMessage", "User created successfully");
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        model.addAttribute("user", user);
        model.addAttribute("roles", userService.findAllRoles());
        return "users/edit-form";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User user,
                             RedirectAttributes redirectAttributes) {
        userService.updateUser(id, user);
        redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
        return "redirect:/users";
    }

    @PostMapping("/change-password/{id}")
    public String changePassword(@PathVariable Long id,
                                 @RequestParam String newPassword,
                                 RedirectAttributes redirectAttributes) {
        userService.changePassword(id, newPassword);
        redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully");
        return "redirect:/users";
    }

    @PostMapping("/change-role/{id}")
    public String changeRole(@PathVariable Long id,
                             @RequestParam String roleName,
                             RedirectAttributes redirectAttributes) {
        userService.changeRole(id, roleName);
        redirectAttributes.addFlashAttribute("successMessage", "Role changed successfully");
        return "redirect:/users";
    }

    @PostMapping("/disable/{id}")
    public String disableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.disableUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User disabled successfully");
        return "redirect:/users";
    }

    @PostMapping("/enable/{id}")
    public String enableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.enableUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User enabled successfully");
        return "redirect:/users";
    }
}