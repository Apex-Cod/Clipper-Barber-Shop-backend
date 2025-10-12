package apex.code.clipperBarberShop.shared;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private String status; // e.g., "success" or "error"
    private String mensaje; // user-facing message (Spanish as requested)
    private T data;

    public static <T> ApiResponse<T> success(String mensaje, T data){
        return ApiResponse.<T>builder()
                .status("success")
                .mensaje(mensaje)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String mensaje, T data){
        return ApiResponse.<T>builder()
                .status("error")
                .mensaje(mensaje)
                .data(data)
                .build();
    }
}
