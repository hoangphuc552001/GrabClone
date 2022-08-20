import axios from "axios"
import {coordinate} from "../../@types/map";

class BookingService {
  autoComplete(address: String) {
    return axios.get(`https://api.geoapify.com/v1/geocode/autocomplete?text=${address}&apiKey=ce49dd1f7cb14bce8674f53bb21ee1c7`)
  }
  convertCoordinateToAddress(coordinate: coordinate) {
    return axios.get(`https://api.geoapify.com/v1/geocode/reverse?lat=${coordinate.latitude}&lon=${coordinate.longitude}&lang=fr&apiKey=ce49dd1f7cb14bce8674f53bb21ee1c7`)
  }
}


export default new BookingService()